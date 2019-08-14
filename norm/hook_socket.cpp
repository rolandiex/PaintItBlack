#include "stdafx.h"

#include "hook_socket.h"
#include "detours.h"


#pragma warning(disable : 26440) // Suppress "noexcept" warning

/* debug */
static std::shared_ptr<norm_dll::norm> c_state;

//#if (CLIENT_VER == 20180620 || CLIENT_VER == 20180621)
#ifdef COMENC
int(WINAPI* pSend)(SOCKET s, const char* buf, int len, int flags) = send;
int(WINAPI* pRecv)(SOCKET s, char* buf, int len, int flags) = recv;

int WINAPI send_hook(SOCKET s, const char* buf, int len, int flags)
{
    // this is a PoC encryption with 1234 as key
    if (s != *c_state->dbg_sock->get_sock()) {
        char* tmp_buf = (char*)calloc(len, sizeof(char));
        for (int i = 0; i < len; i++)
            tmp_buf[i] = buf[i] ^ 1234;
        return pSend(s, tmp_buf, len, flags);
    }
	return pSend(s, buf, len, flags);
}

int WINAPI recv_hook(SOCKET s, char* buf, int len, int flags)
{
    // this is a PoC decryption with 1234 as key
    int ret_len = pRecv(s, buf, len, flags);
    for (int i = 0; i < ret_len; i++)
        buf[i] ^= 1234;
    return ret_len;
}
#endif /* COMENC */
//#endif /* 20180620 */

DWORD CRagConnection__SendPacket_func = 0x007EDA10;
constexpr int BUF_SIZE = 256;

char __fastcall CRagConnection__SendPacket_hook(void* this_obj, DWORD EDX, int count, int source)
{
	CRagConnection__SendPacket original_sendmsg = (CRagConnection__SendPacket)CRagConnection__SendPacket_func;

	char buf[BUF_SIZE];
	int content_size = 0;
	sprintf_s(buf, "Packet data: ");
	content_size = strlen(buf) + count;
	if (content_size * 5 > BUF_SIZE - 1) {
		c_state->dbg_sock->do_send("SendPacket_hook: Buffer was too small!");
	}
	else {
		for (int i = 0; i < count; i++) {
			char tmp[10];
			sprintf_s(tmp, "0x%x ", *reinterpret_cast<BYTE*>(source + i));
			strcat_s(buf, tmp);
		}
		c_state->dbg_sock->do_send(buf);
	}

	return (original_sendmsg)(this_obj, count, source);
}

int socket_detour(std::shared_ptr<norm_dll::norm> state_)
{
    int err = 0;
    int hook_count = 0;
    char info_buf[256];
    c_state = state_;

	err = DetourAttach(&(LPVOID&)CRagConnection__SendPacket_func, &CRagConnection__SendPacket_hook);
	CHECK(info_buf, err);
	if (err == NO_ERROR) {
		hook_count++;
	}
	else
		c_state->dbg_sock->do_send(info_buf);

#ifdef COMENC
    err = DetourAttach(&(LPVOID&)pSend, &send_hook);
    CHECK(info_buf, err);
    c_state->dbg_sock->do_send(info_buf);
    err = DetourAttach(&(LPVOID&)pRecv, &recv_hook);
    CHECK(info_buf, err);
    c_state->dbg_sock->do_send(info_buf);
    hook_count++;
#endif

    sprintf_s(info_buf, "Socket hooks available: %d", hook_count);
    c_state->dbg_sock->do_send(info_buf);

    return hook_count;
}

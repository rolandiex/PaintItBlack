#include "stdafx.h"

#include "hook_bookwnd.h"

#include "norm.h"
#include "detours.h"

namespace norm_dll {
lpSendMsg ProxyUIBookWnd::SendMsg =	reinterpret_cast<lpSendMsg>(UIBOOKWND_SENDMSG_FN);

void __fastcall ProxyUIBookWnd::pSendMsg(void *this_obj, DWORD EDX, int a1, int a2, char *a3, int a4, int a5, int a6)
{
	ProxyUIBookWnd* proxy = *(ProxyUIBookWnd**)((DWORD)this_obj - 4);
	proxy->test();
	SendMsg(this_obj, a1, a2, a3, a4, a5, a6);
}

ProxyUIBookWnd::ProxyUIBookWnd(std::shared_ptr<norm_dll::norm> c_state, UIFrameWnd * c_bookwnd) :
c_state(c_state), c_bookwnd(reinterpret_cast<CUIBookWnd*>(c_bookwnd)) {
	ProxyUIBookWnd** proxy = (ProxyUIBookWnd**)((DWORD)c_bookwnd - 4);
	char buf[64];
	sprintf_s(buf, "proxy address 0x%x and 0x%x, bookwnd address 0x%x,", (DWORD)proxy, (DWORD)*proxy, (DWORD)c_bookwnd);
	c_state->dbg_sock->do_send(buf);
	*proxy = this;
}

void ProxyUIBookWnd::open_book()
{
	char* id = (char*)"pib";
	SendMsg(c_bookwnd, 0x0, 0x5E, id, 0xFD, 0x0, 0x0);
	char* item_name = reinterpret_cast<char*>(c_bookwnd->book_title);
	snprintf(item_name, 64, "Paint it Black Guide");
}

bool ProxyUIBookWnd::hooked = false;
void ProxyUIBookWnd::hook(std::shared_ptr<norm_dll::norm> c_state)
{
	if (hooked)
		return;

	LONG err = 0;
	int hook_count = 0;
	char info_buf[256];

	err = DetourAttach(&(LPVOID&)SendMsg, pSendMsg);
	CHECK(info_buf, err);
	if (err == NO_ERROR) {
		hook_count++;
	}
	else
		c_state->dbg_sock->do_send(info_buf);

	sprintf_s(info_buf, "UIBookWnd hooks available: %d", hook_count);
	c_state->dbg_sock->do_send(info_buf);

	hooked = true;
}
void ProxyUIBookWnd::test() {
	this->c_state->dbg_sock->do_send("Test function called!");
}
}
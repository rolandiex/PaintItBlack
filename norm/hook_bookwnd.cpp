#include "stdafx.h"

#include "hook_bookwnd.h"

#include "norm.h"
#include "detours.h"

namespace norm_dll {
void ProxyUIBookWnd::open_book()
{
	char* id = (char*)"pib";
#if ((CLIENT_VER <= 20180919 && CLIENT_VER >= 20180620) || CLIENT_VER_RE == 20180621)
	SendMsg(c_bookwnd, 0x0, 0x5E, id, 0x108, 0x0, 0x0);
#else
	SendMsg(c_bookwnd, 0x0, 0x5E, id, 0xFD, 0x0, 0x0);
#endif
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

	sprintf_s(info_buf, "UIBookWnd hooks available: %d", hook_count);
	c_state->dbg_sock->do_send(info_buf);

	hooked = true;
}
}
#include "stdafx.h"

#include "hook_windowmgr.h"

#include "detours.h"
#include "norm.h"

namespace norm_dll {
void ProxyUIWindowMgr::hook(std::shared_ptr<norm_dll::norm> c_state)
{
	// install subhooks.
	ProxyUIBookWnd::hook(c_state);

	if (hooked)
		return;

	this->c_state = c_state;

	LONG err = 0;
	int hook_count = 0;
	char info_buf[256];

	sprintf_s(info_buf, "UIWindowMgr hooks available: %d", hook_count);
	this->c_state->dbg_sock->do_send(info_buf);

	this->hooked = true;
}

std::shared_ptr<ProxyUIBookWnd> ProxyUIWindowMgr::make_window()
{
	UIFrameWnd* c_bookwnd = MakeWindow((void*)c_windowmgr, 0x6a);
	this->p_bookwnd = std::make_shared<ProxyUIBookWnd>(this->c_state, c_bookwnd);
	return this->p_bookwnd;
}
}

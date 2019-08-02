#include "stdafx.h"
#include "hook_windowmgr.h"

void norm_dll::ProxyUIWindowMgr::hook(std::shared_ptr<norm_dll::norm> c_state)
{
	// install subhooks.
	ProxyUIBookWnd::hook(c_state);
}

std::shared_ptr<norm_dll::ProxyUIBookWnd> norm_dll::ProxyUIWindowMgr::make_window()
{
	UIFrameWnd* c_bookwnd = MakeWindow((void*)c_windowmgr, 0x6a);
	this->p_bookwnd = std::make_shared<ProxyUIBookWnd>(this->c_state, c_bookwnd);
	return this->p_bookwnd;
}

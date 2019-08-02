#pragma once

#include "client_ver.h"
#include "singleton.h"
#include "hook_bookwnd.h"

#include <memory>

namespace norm_dll {
	class norm;
	class ProxyUIWindowMgr final : public Singleton<ProxyUIWindowMgr> {
	private:
		std::shared_ptr<norm_dll::norm> c_state;

		struct CUIWindowMgr {
			/* Class layout. Can be found in the ClientDate files.*/
		};

		CUIWindowMgr* c_windowmgr = reinterpret_cast<CUIWindowMgr*>(UIWINDOWMGR_PTR);
		bool hooked = false;
		std::shared_ptr<ProxyUIBookWnd> p_bookwnd;

		lpMakeWindow MakeWindow = reinterpret_cast<lpMakeWindow>(UIWINDOWMGR_MAKEWINDOW_FN);
		ProxyUIWindowMgr() {}

	public:
		ProxyUIWindowMgr(token) {}
		~ProxyUIWindowMgr() {}

		void hook(std::shared_ptr<norm_dll::norm> c_state);
		std::shared_ptr<ProxyUIBookWnd> make_window();
	};
}

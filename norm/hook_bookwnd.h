#pragma once

#include "client_ver.h"
#include "singleton.h"

#include <memory>

namespace norm_dll {
	class norm;
	class ProxyUIBookWnd {
	private:
		std::shared_ptr<norm_dll::norm> c_state;

		//void init() { c_bookwnd = nullptr; }

		struct CUIBookWnd : UIFrameWnd {
			/* Class layout. Can be found in the ClientDate files.*/
			UIBOOKWND_DATA
		};

		CUIBookWnd* c_bookwnd = nullptr;
		bool hooked = false;
		
		lpSendMsg SendMsg = reinterpret_cast<lpSendMsg>(UIBOOKWND_SENDMSG_FN);
		ProxyUIBookWnd() {}

	public:
		ProxyUIBookWnd(std::shared_ptr<norm_dll::norm> c_state, UIFrameWnd* c_bookwnd) : 
			c_state(c_state), c_bookwnd(reinterpret_cast<CUIBookWnd*>(c_bookwnd)) {}
		~ProxyUIBookWnd() {}

		void open_book();

		static void hook(std::shared_ptr<norm_dll::norm> c_state);

	};
}

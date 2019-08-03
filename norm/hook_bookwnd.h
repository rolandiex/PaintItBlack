#pragma once

#include "client_ver.h"

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
		static bool hooked;

		static lpSendMsg SendMsg;

		static void __fastcall pSendMsg(void*, DWORD, int, int, char*, int, int, int);
		ProxyUIBookWnd() {}

	public:
		ProxyUIBookWnd(std::shared_ptr<norm_dll::norm> c_state, UIFrameWnd* c_bookwnd);
		~ProxyUIBookWnd() {}

		void open_book();
		void test();

		static void hook(std::shared_ptr<norm_dll::norm> c_state);

	};
}

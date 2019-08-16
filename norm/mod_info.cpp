#include "stdafx.h"

#include "mod_info.h"
#include "norm.h"

info::info(norm_dll::norm* c_state, json* config)
	: mod(c_state)
{
}

info::~info()
{
}

int info::get_talk_type(char* src, int* retval)
{	
	if (strcmp(src, "/pib") == 0) {
		auto book_wnd = p_windowmgr.make_window();
		book_wnd->open_book();
		*retval = -1;
		return 1;
	}
	return 0;
}

#if ((CLIENT_VER <= 20180919 && CLIENT_VER >= 20180620) || CLIENT_VER_RE == 20180621)
void info::send_msg_after(void** this_obj, int* a1, void** a2, int*	  a3, int* a4, int* a5)
#elif CLIENT_VER == 20150000
void info::send_msg_after(void** this_obj, int* a1, int*   a2, int*   a3, int* a4, int* a5)
#endif
{
	if (*a1 != 1)
		return;

	if (!init) {
		init = true;
		char buf[] = { "Paint it Black commands: /pib" };
		print_to_chat(buf);
	}
}

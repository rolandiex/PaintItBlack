#include "stdafx.h"
#include "hook_bookwnd.h"

void norm_dll::ProxyUIBookWnd::open_book()
{
	char* id = (char*)"pib";
	SendMsg(c_bookwnd, 0x0, 0x5E, id, 0xFD, 0x0, 0x0);
	char* item_name = reinterpret_cast<char*>(c_bookwnd->book_title);
	snprintf(item_name, 64, "Paint it Black Guide");
}

void norm_dll::ProxyUIBookWnd::hook(std::shared_ptr<norm_dll::norm> c_state)
{
}

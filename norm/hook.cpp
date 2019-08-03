#include "stdafx.h"

#include "hook.h"
#include "norm.h"
#include "detours.h"

#include <chrono>

namespace norm_dll {
void print_time(norm_dll::norm* c_state)
{
	using namespace std::chrono;
	milliseconds ms = duration_cast<milliseconds>(
		system_clock::now().time_since_epoch()
		);
	char buf[64];
	long long time = ms.count();
	sprintf_s(buf, "%lld", time);
	c_state->dbg_sock->do_send(buf);
}

std::shared_ptr<norm_dll::norm> c_state_;
void* __cdecl pnew(unsigned int size) {
	void* res = calloc(1, size + 4);
	return (void*)((DWORD)res + 4);
}

void __cdecl pdelete(void* target) {
	free((void*)((DWORD)target - 4));
}

void MemoryManager::hook(std::shared_ptr<norm_dll::norm> c_state)
{
	if (this->hooked)
		return;

	c_state_ = c_state;

	LONG err = 0;
	int hook_count = 0;
	char info_buf[256];

	err = DetourAttach(&(LPVOID&)new_, pnew);
	CHECK(info_buf, err);
	if (err == NO_ERROR) {
		hook_count++;
	}
	else
		c_state->dbg_sock->do_send(info_buf);


	err = DetourAttach(&(LPVOID&)delete_, pdelete);
	CHECK(info_buf, err);
	if (err == NO_ERROR) {
		hook_count++;
	}
	else
		c_state->dbg_sock->do_send(info_buf);

	sprintf_s(info_buf, "Memory hooks available: %d", hook_count);
	c_state->dbg_sock->do_send(info_buf);

	this->hooked = true;
}
}

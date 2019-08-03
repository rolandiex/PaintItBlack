#pragma once
#include <memory>

namespace norm_dll {
	class norm;
	void print_time(norm_dll::norm*);

	// Hooking new and delete
	typedef void* (__cdecl* proxy_new)(unsigned int);
	typedef void (__cdecl* proxy_delete)(void*);

	class MemoryManager {
	private:
		proxy_new new_ = reinterpret_cast<proxy_new>(0x0096C98C);
		proxy_delete delete_ = reinterpret_cast<proxy_delete>(0x0096C992);
		bool hooked = false;

	public:
		void hook(std::shared_ptr<norm_dll::norm> c_state);
	};
}
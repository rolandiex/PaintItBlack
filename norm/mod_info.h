#pragma once
#include "mod.h"

class info : public norm_dll::mod {
public:
	info(norm_dll::norm* c_state, json* config);
	virtual ~info();

	bool init = false;
	bool welcome = false;

	void send_msg_after(void**, int*, void**, int*, int*, int*);
	void send_msg_after(void**, int*, int*, int*, int*, int*);
	int get_talk_type(char*, int*);
};

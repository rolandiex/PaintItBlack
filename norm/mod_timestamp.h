#pragma once
#include "mod.h"
class timestamp :
	public norm_dll::mod
{
public:
    timestamp(norm_dll::norm* c_state, json* config);
	virtual ~timestamp();
	void send_msg(void**, int*, void**, int*, int*, int*);
	void send_msg(void**, int*, int*, int*, int*, int*);
	void get_current_setting(json& setting);

private:
	int get_talk_type(char*, int*);
	int enabled = 1;
    std::string time_format = "%H:%M:%S";
	char msg_buf[256] = { 0 };
};


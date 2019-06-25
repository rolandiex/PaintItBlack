#pragma once
#include "mod.h"

class config : public norm_dll::mod {
public:
    config(norm_dll::norm* c_state, json* config);
    virtual ~config();

	int get_talk_type(void**, void**, int*, int*, int*);
    int get_talk_type(void**, char**, int*, char**, int*);
};

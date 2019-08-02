#pragma once
#include "mod.h"
#include "mod_graphics.h"

class overlay_new :
	public norm_dll::mod
{
public:
    overlay_new(norm_dll::norm* c_state, std::shared_ptr<graphics> g, json* config);
	virtual ~overlay_new();
	void get_current_setting(json& setting);
    
private:
	int initialized = 0;
	int display_ping = 0;
    int fps_conf = 0;
	int display_fps = 0;
	/* postion of the overlay */
	int x = -1;
	int y = -1;

	std::shared_ptr<graphics> g;

	HRESULT end_scene(IDirect3DDevice7**);
    int get_talk_type(char*, int*);
	void draw_scene(void * this_obj);
};


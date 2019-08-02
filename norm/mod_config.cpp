#include "stdafx.h"

#include "mod_config.h"
#include "norm.h"

#include <fstream>
#include <iomanip>

config::config(norm_dll::norm* c_state, json* config)
    : mod(c_state)
{
}

config::~config()
{
}

int config::get_talk_type(char* src, int* retval)
{
    if (strcmp(src, "/savesettings") == 0) {
        json settings;
        for (auto mod_ : c_state->mods) {
            if (strcmp(typeid(*mod_).name() + 6, "config") == 0)
                continue; // skip mod_config settings
            mod_->get_current_setting(settings["mods"][typeid(*mod_).name() + 6]);
        }
        c_state->dbg_sock->do_send(settings.dump().c_str());
        std::ofstream o("pib_config.json");
        o << std::setw(4) << settings << std::endl;

        char buf[256];
		sprintf_s(buf, "Current settings are now saved.");
        this->print_to_chat(buf);
        *retval = -1;
        return 1;
    }

	return 0;
}

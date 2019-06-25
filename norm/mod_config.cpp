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

#if ((CLIENT_VER <= 20180919 && CLIENT_VER >= 20180620) || CLIENT_VER_RE == 20180621)
int config::get_talk_type(void** this_obj, void** src, int* a1, int* a2, int* retval)
#elif CLIENT_VER == 20150000
int config::get_talk_type(void** this_obj, char** src, int* a1, char** a2, int* retval)
#endif
{
    if (strcmp((char*)*src, "/savesettings") == 0) {
        json settings;
        for (auto mod_ : c_state->mods) {
            if (strcmp(typeid(*mod_).name() + 6, "config") == 0)
                continue; // skip mod_config settings
            mod_->get_current_setting(settings["mods"][typeid(*mod_).name() + 6]);
        }
        c_state->dbg_sock->do_send(settings.dump().c_str());
        std::ofstream o("pib_test_config.json");
        o << std::setw(4) << settings << std::endl;

        char buf[256];
		sprintf_s(buf, "Current settings are now saved.");
        this->print_to_chat(buf);
        *retval = -1;
        return 1;
    }

	return 0;
}

#pragma once
#include "client_ver.h"
#include "debug_socket.h"
#include "hook_renderer.h"
#include "hook_session.h"
#include "hook_windowmgr.h"
#include "json.h"
#define INITGUID
#include <d3d.h>

#pragma warning(disable : 26440) // Suppress "noexcept" warning

using json = nlohmann::json;

namespace norm_dll {
class norm;
class mod : public std::enable_shared_from_this<mod> {
public:
    mod(norm* c_state)
        : c_state(c_state)
    {
    }
    virtual ~mod() {}

protected:
    ProxySession& p_session = ProxySession::instance();
    ProxyRenderer& p_renderer = ProxyRenderer::instance();
	ProxyUIWindowMgr& p_windowmgr = ProxyUIWindowMgr::instance();
    norm* c_state;
    void print_to_chat(char* msg);

public: //hide later with private and friendclass
    virtual void get_current_setting(json& setting) { setting = json({}); };
#if ((CLIENT_VER <= 20180919 && CLIENT_VER >= 20180620) || CLIENT_VER_RE == 20180621)
    virtual void send_msg(void**, int*, void**, int*, int*, int*)
    {
    }
#elif CLIENT_VER == 20150000
    virtual void send_msg(void**, int*, int*, int*, int*, int*)
    {
    }
#endif
#if ((CLIENT_VER <= 20180919 && CLIENT_VER >= 20180620) || CLIENT_VER_RE == 20180621)
	virtual void send_msg_after(void**, int*, void**, int*, int*, int*)
	{
	}
#elif CLIENT_VER == 20150000
	virtual void send_msg_after(void**, int*, int*, int*, int*, int*)
	{
	}
#endif
    virtual HRESULT begin_scene(IDirect3DDevice7**)
    {
        return E_NOTIMPL;
    }
    virtual HRESULT end_scene(IDirect3DDevice7**) { return E_NOTIMPL; }
    virtual HRESULT WaitForVerticalBlank(DWORD*, HANDLE*) { return E_NOTIMPL; };
    virtual void ddraw_release() {}

	/**
	* init:
	*	called right after creating the CProxyIDirect3DDevice7.
	*	This happens when the client is about to display on startup.
	*   All load heavy work should be done in init.
	*	PiB logo is displayed until all calls to init are done, then the client window is displayed.
	**/
	virtual void init() {}

    int active = -1;

private:
    /* Renderer callbacks */
    friend class ProxyRenderer;
    virtual void draw_scene(void*) {}

    /* Session callbacks */
    friend class ProxySession;
    virtual int get_talk_type(char* msg, int* retval)
    {
        return 0;
    }
};
}

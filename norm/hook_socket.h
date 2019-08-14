#pragma once
#include "stdafx.h"
#include "client_ver.h"
#include "debug_socket.h"
#include "norm.h"
#include <Windows.h>

int socket_detour(std::shared_ptr<norm_dll::norm> c_state);

typedef char(__thiscall *CRagConnection__SendPacket)(void*, int count, int source);


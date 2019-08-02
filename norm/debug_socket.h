#pragma once
#include <string>
#include <winsock2.h>

namespace norm_dll {

class debug_socket {
public:
    int disabled = 0;
    debug_socket();
    debug_socket(std::string ip, std::string port);
    virtual ~debug_socket();

    int do_connect();
    int do_send(const char* sendbuf);
    SOCKET* get_sock();

private:
    std::string port = "1337";
    std::string ip = "192.168.178.26";
    SOCKET ConnectSocket = INVALID_SOCKET;
};
}

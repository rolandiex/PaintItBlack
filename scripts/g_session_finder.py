#This script searches for:
# - g_session
#@author norm
#@category _NEW_
#@keybinding 
#@menupath 
#@toolbar 

# GLOBAL DEFINES
DEBUG = False
cm = currentProgram.getCodeManager()
rm = currentProgram.getReferenceManager()
af = currentProgram.getAddressFactory()

# debug print
def dprint(msg):
	if DEBUG:
		print msg


# helper function
def refIterToAddrList(refIter):
	addr_lst = []

	for entry in refIter:
		addr_lst.append(af.getAddress(entry.toString().split(" ")[1]))

	return addr_lst


# Trying to find g_session.
# Looking for:
#	- functions which reference "PingLog.txt"
#	- instruction with "[EBP + 0x8]"
# 	- instruction with "MOV ECX,0x"
def find_g_session():
	addr = find("PingLog.txt")
	iter = currentProgram.getReferenceManager().getReferencesTo(addr)
	addr_lst = refIterToAddrList(iter)
	find_count = 0

	for addr in addr_lst:
		instr_lst = []
		found_ebp = False
		count = 0

		# search a bit.
		addr_set = af.getAddressSet(addr, addr.add(250))

		for entry in cm.getInstructions(addr_set, True):

			# probably function end. we can stop here
			if entry.toString().find("POP EBP") != -1:
				dprint(entry.getMinAddress())
				break

			# first need to find this
			if entry.toString().find("[EBP + 0x8]") != -1 and not found_ebp:
				found_ebp = True

			# finally need to find that
			if found_ebp and entry.toString().find("MOV ECX,0x") != -1:
				find_count += 1
				print entry.getMinAddress()
				break

			instr_lst.append(entry.toString())
			count += 1

		dprint(instr_lst[-10:])

	dprint(find_count)
	

if __name__ == "__main__":
	find_g_session()


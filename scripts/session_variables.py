#TODO write a description for this script
#@author 
#@category _NEW_
#@keybinding 
#@menupath 
#@toolbar 


cm = currentProgram.getCodeManager()
rm = currentProgram.getReferenceManager()
af = currentProgram.getAddressFactory()

def find_aid()
	aid_range = findBytes(None, '\\xe8.{4}\\x8b.{5}\\x3b.{5}\\x75.{1}\\xb9.{4}\\xe8.{4}\\x8b.{5}\\x8b.{2}')

if __name__ == "__main__":
	find_aid();
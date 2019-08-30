//Core of the helper classes
//@author Norman Ziebal
//@category pib.lib
//@keybinding 
//@menupath 
//@toolbar 

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;
import ghidra.program.model.mem.MemoryAccessException;
import ghidra.program.model.symbol.ReferenceIterator;
import ghidra.program.flatapi.FlatProgramAPI;

enum LISTTYPE {
	ARRAYLIST,
	SET
}

public class PiBLib {
	Logger logger;
	GhidraScript caller;
	
	public PiBLib(boolean debug, String dfunc, GhidraScript caller) {
		logger = new Logger(debug, dfunc, caller);
		this.caller = caller;
	}

	public <T> void log(T msg) {
		logger.print(msg);
	}
	
	/* simple hex to 'signature' */
	public String toSignature(String signature) {
		return "\\x" + signature.replace(" ", "\\x");
	}
	
	public Object string_to_reflist(LISTTYPE t, String str) {
		switch(t) {
		case ARRAYLIST:
			return string_to_reflist_arr(str);
		case SET:
			return string_to_reflist_set(str);
		default:
			caller.printerr("Unknown LISTTYPE");
		}
		return null;
	}
	
	public ArrayList<Function> string_to_reflist_arr(String str) {
		Address str_addr = caller.find(str);
		log("PiBLib.string_to_reflist: str_addr: 0x" + str_addr);
		ReferenceIterator iter = caller.getCurrentProgram().getReferenceManager().getReferencesTo(str_addr);
		
		ArrayList<Function> func_set = new ArrayList<>();
		FunctionManager fm = caller.getCurrentProgram().getFunctionManager();
		for(ReferenceIterator it = iter; it.hasNext();) {
			func_set.add(fm.getFunctionContaining(it.next().getFromAddress()));
		}
		return func_set;
	}
	
	public Set<Function> string_to_reflist_set(String str) {
		Address str_addr = caller.find(str);
		log("PiBLib.string_to_reflist: str_addr: 0x" + str_addr);
		ReferenceIterator iter = caller.getCurrentProgram().getReferenceManager().getReferencesTo(str_addr);
		
		Set<Function> func_set = new HashSet<>();
		FunctionManager fm = caller.getCurrentProgram().getFunctionManager();
		for(ReferenceIterator it = iter; it.hasNext();) {
			func_set.add(fm.getFunctionContaining(it.next().getFromAddress()));
		}
		return func_set;
	}
	
	public int checkDate() throws MemoryAccessException {
		/* Check date */
		String ragexe = toSignature("44 3a 5c 53 6f 75 72 63 65 5c 4b 6f 72 65 61 5c 52 4f 5f");
		log("Looking for: " + ragexe);
		
		Address ragexe_addr = caller.findBytes(null, ragexe);
		if(ragexe_addr == null) {
			caller.printerr("Unable to find client date!");
			return -1;
		}
		
		log("Client date: " + ragexe_addr);
		byte[] bytes = new byte[256];
		int c = 0;
		while(true) {
			if (caller.getByte(ragexe_addr) == 0)
				break;
			
			if (c > 255)
				break;
			
			bytes[c] = caller.getByte(ragexe_addr);
			ragexe_addr = ragexe_addr.next();
			c++;
		}
		String byte_str = new String(bytes, StandardCharsets.UTF_8);
		log("Client date string: " + byte_str);
		
		String client_date_str = null;
		var split_str = byte_str.split("\\\\");
		for (var substr : split_str) {
			log("Substr: " + substr);
			if (substr.startsWith("RO_")) {
				client_date_str = substr.replace("RO_", "").replace("-", "");
				log("Client date number: " + client_date_str);
				break;
			}	
		}
		
		if (client_date_str == null) {
			caller.printerr("Failed to get Client date!");
			return -1;
		}
		
		var client_date_split = client_date_str.split("#");
		return Integer.parseInt(client_date_split[0]);
	}

}

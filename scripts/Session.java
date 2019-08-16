//TODO write a description for this script
//@author 
//@category pib
//@keybinding 
//@menupath 
//@toolbar 

import ghidra.app.script.GhidraScript;
import ghidra.program.model.util.*;
import ghidra.program.model.reloc.*;
import ghidra.program.model.data.*;
import ghidra.program.model.block.*;
import ghidra.program.model.symbol.*;
import ghidra.program.model.scalar.*;
import ghidra.program.model.mem.*;
import ghidra.program.model.listing.*;
import ghidra.program.model.lang.*;
import ghidra.program.model.pcode.*;
import ghidra.program.model.address.*;

public class Session extends GhidraScript {

	public void run() throws Exception {
		/* header and static things */
		print("//\n");
		print("// CSession defines\n");
		print("//\n");
		print("typedef signed int(__thiscall* lpGetTalkType)(void*, char*, int, char*);\n");
		print("typedef void(__thiscall* lpRecalcAveragePingTime)(void*, unsigned long);\n");
		print("\n");
		print("//struct CSession {\n");
		print("#define SESSION_DATA   \\\n");
		print("\tTODO\n");
		print("//};\n");

    }

}

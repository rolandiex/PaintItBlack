//TODO write a description for this script
//@author 
//@category pib
//@keybinding 
//@menupath 
//@toolbar 


import java.util.Iterator;
import java.util.Set;

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

public class session_old extends GhidraScript {
	PiBLib pib;
	
	public void find_RecalcAveragePingTime()
	{
		Set<Function> func_set = pib.string_to_reflist("10.2f");
		if (func_set.size() == 1) {
			Address recalcAveragePingTime_addr = null;
			for(Iterator<Function> it = func_set.iterator(); it.hasNext();)
				recalcAveragePingTime_addr = it.next().getEntryPoint();
			pib.log("find_RecalcAveragePingTime: recalcAveragePingTime: " + recalcAveragePingTime_addr);
			
			// search pop ebp & jmp address
		} else
			printerr("find_GetTalkType: failed because " + func_set.size() + " have been found! Only one is allowed!");
	}
	
	public void find_GetTalkType()
	{
		Set<Function> func_set = pib.string_to_reflist("/goldpc");
		if (func_set.size() == 1) {
			Address GetTalkType_addr = null;
			for(Iterator<Function> it = func_set.iterator(); it.hasNext();)
				GetTalkType_addr = it.next().getEntryPoint();
			
			print("GetTalkType: 0x" + GetTalkType_addr + "\n");
		} else
			printerr("find_GetTalkType: failed because " + func_set.size() + " have been found! Only one is allowed!");
	}
	
    public void run() throws Exception 
    {
    	pib = new PiBLib(false, "find_RecalcAveragePingTime", this);
    	find_GetTalkType();
    	find_RecalcAveragePingTime();
    }

}

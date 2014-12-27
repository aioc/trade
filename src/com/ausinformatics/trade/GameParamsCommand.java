package com.ausinformatics.trade;

import java.io.PrintStream;

import com.ausinformatics.phais.core.Director;
import com.ausinformatics.phais.core.commander.commands.Command;

public class GameParamsCommand implements Command {

	private GameFactory f;
	public GameParamsCommand(GameFactory f) {
		this.f = f;
	}
	
	@Override
	public void execute(Director reportTo, PrintStream out, String[] args) {
		boolean badArgs = false;
		int boardSize = 0;
		int numProd = 0;
		int numCons = 0;
		int numTypes = 0;
		int startMon = 0;
		if (args.length != 5) {
			badArgs = true;
		} else {
			try {
				boardSize = Integer.parseInt(args[0]);
				numProd = Integer.parseInt(args[1]);
				numCons = Integer.parseInt(args[2]);
				numTypes = Integer.parseInt(args[3]);
				startMon = Integer.parseInt(args[4]);
			} catch (NumberFormatException e) {
				badArgs = true;
			}
		}
		
		if (badArgs) {
			out.println("Usage: PARAMS boardSize, numProd, numCons, numTypes, startingMoney");
		} else {
			f.boardSize = boardSize;
			f.initialMoney = startMon;
			f.numConsumers = numCons;
			f.numProducers = numProd;
			f.numTypes = numTypes;
		}
	}

	@Override
	public String shortHelpString() {
		return "Change the params of the games.\nIn order of boardSize, numProd, numCons, numTypes, startingMoney";
	}

	@Override
	public String detailedHelpString() {
		return null;
	}

}

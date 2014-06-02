package io.github.gustav9797.ZombieInvasionMinigame;

import java.io.File;

public class Map
{
	public String name;
	
	
	public Map(String folder)
	{
		File file = new File(folder);
		if(file.isDirectory() && file.exists())
		{
			this.name = folder;
		}
	}
}

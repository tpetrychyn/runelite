/*
 * Copyright (c) 2017, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.cache.definitions.loaders;

import net.runelite.cache.IndexType;
import net.runelite.cache.definitions.MapDefinition;
import net.runelite.cache.definitions.MapDefinition.Tile;
import net.runelite.cache.fs.Archive;
import net.runelite.cache.fs.Index;
import net.runelite.cache.fs.Storage;
import net.runelite.cache.fs.Store;
import net.runelite.cache.io.InputStream;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static net.runelite.cache.region.Region.X;
import static net.runelite.cache.region.Region.Y;
import static net.runelite.cache.region.Region.Z;

public class MapLoader
{
	public MapLoader() {}

	public MapLoader(Store store) {
		this.store = store;
		index = store.getIndex(IndexType.MAPS);

		mapDefCache = new HashMap<>();
	}

	private Store store;
	private Index index;
	private Map<Integer, MapDefinition> mapDefCache;

	public Tile getWorldTile(int z, int x, int y) {
		MapDefinition m = loadFromWorldCoordinates(x, y);
		if (m == null) {
			return null;
		}

		return m.getTiles()[z][x][y];
	}

	public MapDefinition loadFromWorldCoordinates(int x, int y) {
		x >>>= 6;
		y >>>= 6;
		int regionId = (x << 8) | y;
		if (mapDefCache.containsKey(regionId)) {
			return mapDefCache.get(regionId);
		}

		index = store.getIndex(IndexType.MAPS);

		Storage storage = store.getStorage();
		Archive map = index.findArchiveByName("m" + x + "_" + y);
		Archive land = index.findArchiveByName("l" + x + "_" + y);

		if (map == null || land == null)
		{
			return null;
		}

		byte[] data = new byte[0];
		try {
			data = map.decompress(storage.loadArchive(map));
		} catch (IOException e) {
			e.printStackTrace();
		}

		MapDefinition mapDef = new MapLoader().load(x, y, data);

		mapDefCache.put(regionId, mapDef);
		return mapDef;
	}

	public MapDefinition load(int regionX, int regionY, byte[] b)
	{
		MapDefinition map = new MapDefinition();
		map.setRegionX(regionX);
		map.setRegionY(regionY);
		loadTerrain(map, b);
		return map;
	}

	private void loadTerrain(MapDefinition map, byte[] buf)
	{
		Tile[][][] tiles = map.getTiles();

		InputStream in = new InputStream(buf);

		for (int z = 0; z < Z; z++)
		{
			for (int x = 0; x < X; x++)
			{
				for (int y = 0; y < Y; y++)
				{
					Tile tile = tiles[z][x][y] = new Tile();
					while (true)
					{
						int attribute = in.readUnsignedByte();
						if (attribute == 0)
						{
							break;
						}
						else if (attribute == 1)
						{
							int height = in.readUnsignedByte();
							tile.height = height;
							break;
						}
						else if (attribute <= 49)
						{
							tile.attrOpcode = attribute;
							tile.overlayId = in.readByte();
							tile.overlayPath = (byte) ((attribute - 2) / 4);
							tile.overlayRotation = (byte) (attribute - 2 & 3);
						}
						else if (attribute <= 81)
						{
							tile.settings = (byte) (attribute - 49);
						}
						else
						{
							tile.underlayId = (byte) (attribute - 81);
						}
					}
				}
			}
		}
	}
}

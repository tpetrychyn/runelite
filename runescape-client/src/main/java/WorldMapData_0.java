import net.runelite.mapping.Export;
import net.runelite.mapping.Implements;
import net.runelite.mapping.ObfuscatedName;
import net.runelite.mapping.ObfuscatedSignature;

@ObfuscatedName("y")
@Implements("WorldMapData_0")
public class WorldMapData_0 extends AbstractWorldMapData {
	@ObfuscatedName("y")
	@Export("BZip2Decompressor_block")
	static int[] BZip2Decompressor_block;
	@ObfuscatedName("br")
	static String field137;
	@ObfuscatedName("fh")
	@ObfuscatedSignature(
		signature = "Lkx;"
	)
	@Export("fontPlain12")
	static Font fontPlain12;
	@ObfuscatedName("gh")
	@Export("regionLandArchives")
	static byte[][] regionLandArchives;

	WorldMapData_0() {
	}

	@ObfuscatedName("x")
	@ObfuscatedSignature(
		signature = "(Lkb;I)V",
		garbageValue = "-1716650970"
	)
	@Export("init")
	void init(Buffer var1) {
		int var2 = var1.readUnsignedByte();
		if (var2 != WorldMapID.field302.value) {
			throw new IllegalStateException("");
		} else {
			super.minPlane = var1.readUnsignedByte();
			super.planes = var1.readUnsignedByte();
			super.regionXLow = var1.readUnsignedShort();
			super.regionYLow = var1.readUnsignedShort();
			super.regionX = var1.readUnsignedShort();
			super.regionY = var1.readUnsignedShort();
			super.groupId = var1.method5591();
			super.fileId = var1.method5591();
		}
	}

	@ObfuscatedName("m")
	@ObfuscatedSignature(
		signature = "(Lkb;B)V",
		garbageValue = "-78"
	)
	@Export("readGeography")
	void readGeography(Buffer var1) {
		super.planes = Math.min(super.planes, 4);
		super.floorUnderlayIds = new short[1][64][64];
		super.floorOverlayIds = new short[super.planes][64][64];
		super.field205 = new byte[super.planes][64][64];
		super.field206 = new byte[super.planes][64][64];
		super.decorations = new WorldMapDecoration[super.planes][64][64][];
		int var2 = var1.readUnsignedByte();
		if (var2 != class39.field300.value) {
			throw new IllegalStateException("");
		} else {
			int var3 = var1.readUnsignedByte();
			int var4 = var1.readUnsignedByte();
			if (var3 == super.regionX && var4 == super.regionY) {
				for (int var5 = 0; var5 < 64; ++var5) {
					for (int var6 = 0; var6 < 64; ++var6) {
						this.readTile(var5, var6, var1);
					}
				}

			} else {
				throw new IllegalStateException("");
			}
		}
	}

	public boolean equals(Object var1) {
		if (!(var1 instanceof WorldMapData_0)) {
			return false;
		} else {
			WorldMapData_0 var2 = (WorldMapData_0)var1;
			return super.regionX == var2.regionX && var2.regionY == super.regionY;
		}
	}

	public int hashCode() {
		return super.regionX | super.regionY << 8;
	}

	@ObfuscatedName("k")
	@ObfuscatedSignature(
		signature = "([BIIII[Lfm;B)V",
		garbageValue = "-108"
	)
	static final void method275(byte[] data, int regionX, int regionY, int var3, int var4, CollisionMap[] var5) {
		for (int z = 0; z < 4; ++z) {
			for (int x = 0; x < 64; ++x) {
				for (int y = 0; y < 64; ++y) {
					if (x + regionX > 0 && x + regionX < 103 && y + regionY > 0 && y + regionY < 103) {
						int[] var10000 = var5[z].flags[x + regionX];
						var10000[y + regionY] &= -16777217;
					}
				}
			}
		}

		Buffer buffer = new Buffer(data);

		for (int z = 0; z < 4; ++z) {
			for (int x = 0; x < 64; ++x) {
				for (int y = 0; y < 64; ++y) {
					MusicPatchNode2.loadTerrain(buffer, z, x + regionX, y + regionY, var3, var4, 0);
				}
			}
		}

	}

	@ObfuscatedName("ki")
	@ObfuscatedSignature(
		signature = "(I)V",
		garbageValue = "-1448123046"
	)
	static final void method263() {
		Client.field874 = Client.cycleCntr;
		Timer.ClanChat_inClanChat = true;
	}
}

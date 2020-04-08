import net.runelite.mapping.Export;
import net.runelite.mapping.Implements;
import net.runelite.mapping.ObfuscatedGetter;
import net.runelite.mapping.ObfuscatedName;
import net.runelite.mapping.ObfuscatedSignature;

@ObfuscatedName("gf")
@Implements("MusicPatchNode2")
public class MusicPatchNode2 {
	@ObfuscatedName("x")
	byte[] field2402;
	@ObfuscatedName("m")
	byte[] field2398;
	@ObfuscatedName("k")
	@ObfuscatedGetter(
		intValue = -224180223
	)
	int field2396;
	@ObfuscatedName("d")
	@ObfuscatedGetter(
		intValue = 227896201
	)
	int field2397;
	@ObfuscatedName("w")
	@ObfuscatedGetter(
		intValue = -615138819
	)
	int field2400;
	@ObfuscatedName("v")
	@ObfuscatedGetter(
		intValue = 1353186567
	)
	int field2399;
	@ObfuscatedName("q")
	@ObfuscatedGetter(
		intValue = 22420513
	)
	int field2395;
	@ObfuscatedName("z")
	@ObfuscatedGetter(
		intValue = 2050807293
	)
	int field2401;
	@ObfuscatedName("t")
	@ObfuscatedGetter(
		intValue = -1299814217
	)
	int field2394;

	MusicPatchNode2() {
	}

	@ObfuscatedName("m")
	@ObfuscatedSignature(
		signature = "(Liy;III)Lla;",
		garbageValue = "273345835"
	)
	@Export("SpriteBuffer_getSprite")
	public static Sprite SpriteBuffer_getSprite(AbstractArchive var0, int var1, int var2) {
		return !UserComparator7.method3516(var0, var1, var2) ? null : ArchiveDisk.method5932();
	}

	@ObfuscatedName("w")
	@ObfuscatedSignature(
		signature = "(Lkb;IIIIIIB)V",
		garbageValue = "16"
	)
	@Export("loadTerrain")
	static final void loadTerrain(Buffer buffer, int z, int x, int y, int var4, int var5, int var6) {
		int var7;
		if (x >= 0 && x < 104 && y >= 0 && y < 104) {
			Tiles.Tiles_renderFlags[z][x][y] = 0;

			while (true) {
				var7 = buffer.readUnsignedByte();
				if (var7 == 0) {
					if (z == 0) {
						int[] var8 = Tiles.Tiles_heights[0][x];
						int var11 = x + var4 + 932731;
						int var12 = y + var5 + 556238;
						int var13 = Canvas.method957(var11 + 45365, 91923 + var12, 4) - 128 + (Canvas.method957(10294 + var11, 37821 + var12, 2) - 128 >> 1) + (Canvas.method957(var11, var12, 1) - 128 >> 2);
						var13 = (int)(0.3D * (double)var13) + 35;
						if (var13 < 10) {
							var13 = 10;
						} else if (var13 > 60) {
							var13 = 60;
						}

						var8[y] = -var13 * 8;
					} else {
						Tiles.Tiles_heights[z][x][y] = Tiles.Tiles_heights[z - 1][x][y] - 240;
					}
					break;
				}

				if (var7 == 1) {
					int var14 = buffer.readUnsignedByte();
					if (var14 == 1) {
						var14 = 0;
					}

					if (z == 0) {
						Tiles.Tiles_heights[0][x][y] = -var14 * 8;
					} else {
						Tiles.Tiles_heights[z][x][y] = Tiles.Tiles_heights[z - 1][x][y] - var14 * 8;
					}
					break;
				}

				if (var7 <= 49) {
					class348.overlayIds[z][x][y] = buffer.readByte();
					Tiles.overlayPaths[z][x][y] = (byte)((var7 - 2) / 4);
					Tiles.overlayRotations[z][x][y] = (byte)(var7 - 2 + var6 & 3);
				} else if (var7 <= 81) {
					Tiles.Tiles_renderFlags[z][x][y] = (byte)(var7 - 49);
				} else {
					Tiles.underlayIds[z][x][y] = (byte)(var7 - 81);
				}
			}
		} else {
			while (true) {
				var7 = buffer.readUnsignedByte();
				if (var7 == 0) {
					break;
				}

				if (var7 == 1) {
					buffer.readUnsignedByte();
					break;
				}

				if (var7 <= 49) {
					buffer.readUnsignedByte();
				}
			}
		}

	}
}

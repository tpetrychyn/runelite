import net.runelite.mapping.Export;
import net.runelite.mapping.Implements;
import net.runelite.mapping.ObfuscatedGetter;
import net.runelite.mapping.ObfuscatedName;
import net.runelite.mapping.ObfuscatedSignature;

@ObfuscatedName("kj")
@Implements("PacketBuffer")
public class PacketBuffer extends Buffer {
	@ObfuscatedName("m")
	static final int[] field3707;
	@ObfuscatedName("x")
	@ObfuscatedSignature(
		signature = "Llp;"
	)
	@Export("isaacCipher")
	IsaacCipher isaacCipher;
	@ObfuscatedName("k")
	@ObfuscatedGetter(
		intValue = -2144680439
	)
	@Export("bitIndex")
	int bitIndex;

	static {
		field3707 = new int[]{0, 1, 3, 7, 15, 31, 63, 127, 255, 511, 1023, 2047, 4095, 8191, 16383, 32767, 65535, 131071, 262143, 524287, 1048575, 2097151, 4194303, 8388607, 16777215, 33554431, 67108863, 134217727, 268435455, 536870911, 1073741823, Integer.MAX_VALUE, -1};
	}

	public PacketBuffer(int var1) {
		super(var1);
	}

	@ObfuscatedName("x")
	@ObfuscatedSignature(
		signature = "([II)V",
		garbageValue = "-1491722671"
	)
	@Export("newIsaacCipher")
	public void newIsaacCipher(int[] var1) {
		this.isaacCipher = new IsaacCipher(var1);
	}

	@ObfuscatedName("m")
	@ObfuscatedSignature(
		signature = "(Llp;I)V",
		garbageValue = "452312441"
	)
	@Export("setIsaacCipher")
	public void setIsaacCipher(IsaacCipher var1) {
		this.isaacCipher = var1;
	}

	@ObfuscatedName("k")
	@ObfuscatedSignature(
		signature = "(II)V",
		garbageValue = "-407468573"
	)
	@Export("writeByteIsaac")
	public void writeByteIsaac(int var1) {
		super.array[++super.offset - 1] = (byte)(var1 + this.isaacCipher.nextInt());
	}

	@ObfuscatedName("d")
	@ObfuscatedSignature(
		signature = "(I)I",
		garbageValue = "-1676648466"
	)
	@Export("readByteIsaac")
	public int readByteIsaac() {
		return super.array[++super.offset - 1] - this.isaacCipher.nextInt() & 255;
	}

	@ObfuscatedName("w")
	@ObfuscatedSignature(
		signature = "(B)Z",
		garbageValue = "36"
	)
	public boolean method5524() {
		int var1 = super.array[super.offset] - this.isaacCipher.method6364() & 255;
		return var1 >= 128;
	}

	@ObfuscatedName("v")
	@ObfuscatedSignature(
		signature = "(I)I",
		garbageValue = "787135806"
	)
	@Export("readSmartByteShortIsaac")
	public int readSmartByteShortIsaac() {
		int var1 = super.array[++super.offset - 1] - this.isaacCipher.nextInt() & 255;
		return var1 < 128 ? var1 : (var1 - 128 << 8) + (super.array[++super.offset - 1] - this.isaacCipher.nextInt() & 255);
	}

	@ObfuscatedName("q")
	@ObfuscatedSignature(
		signature = "([BIII)V",
		garbageValue = "330744535"
	)
	public void method5516(byte[] var1, int var2, int var3) {
		for (int var4 = 0; var4 < var3; ++var4) {
			var1[var4 + var2] = (byte)(super.array[++super.offset - 1] - this.isaacCipher.nextInt());
		}

	}

	@ObfuscatedName("z")
	@ObfuscatedSignature(
		signature = "(B)V",
		garbageValue = "-6"
	)
	@Export("importIndex")
	public void importIndex() {
		this.bitIndex = super.offset * 8;
	}

	@ObfuscatedName("t")
	@ObfuscatedSignature(
		signature = "(IB)I",
		garbageValue = "-43"
	)
	@Export("readBits")
	public int readBits(int var1) {
		int var2 = this.bitIndex >> 3;
		int var3 = 8 - (this.bitIndex & 7);
		int var4 = 0;

		for (this.bitIndex += var1; var1 > var3; var3 = 8) {
			var4 += (super.array[var2++] & field3707[var3]) << var1 - var3;
			var1 -= var3;
		}

		if (var3 == var1) {
			var4 += super.array[var2] & field3707[var3];
		} else {
			var4 += super.array[var2] >> var3 - var1 & field3707[var1];
		}

		return var4;
	}

	@ObfuscatedName("e")
	@ObfuscatedSignature(
		signature = "(I)V",
		garbageValue = "931642026"
	)
	@Export("exportIndex")
	public void exportIndex() {
		super.offset = (this.bitIndex + 7) / 8;
	}

	@ObfuscatedName("s")
	@ObfuscatedSignature(
		signature = "(IB)I",
		garbageValue = "0"
	)
	@Export("bitsRemaining")
	public int bitsRemaining(int var1) {
		return var1 * 8 - this.bitIndex;
	}

	@ObfuscatedName("v")
	@ObfuscatedSignature(
		signature = "(IIIIIILej;Lfm;S)V",
		garbageValue = "7907"
	)
	static final void addLocationObjectToScene(int z, int x, int y, int var5, int orientation, int type, Scene scene, CollisionMap collisionMap) {
		if (!Client.isLowDetail || (SceneRegion.Tiles_renderFlags[0][x][y] & 2) != 0 || (SceneRegion.Tiles_renderFlags[z][x][y] & 16) == 0) {
			if (z < SceneRegion.Tiles_minPlane) {
				SceneRegion.Tiles_minPlane = z;
			}

			ObjectDefinition objectDefinition = WorldMapSection2.getObjectDefinition(type);
			int width;
			int length;
			if (orientation != 1 && orientation != 3) {
				width = objectDefinition.sizeX;
				length = objectDefinition.sizeY;
			} else {
				width = objectDefinition.sizeY;
				length = objectDefinition.sizeX;
			}

			int var11;
			int var12;
			if (width + x <= 104) {
				var11 = (width >> 1) + x;
				var12 = (width + 1 >> 1) + x;
			} else {
				var11 = x;
				var12 = x + 1;
			}

			int var13;
			int var14;
			if (length + y <= 104) {
				var13 = (length >> 1) + y;
				var14 = y + (length + 1 >> 1);
			} else {
				var13 = y;
				var14 = y + 1;
			}

			int[][] tileHeights = SceneRegion.Tiles_heights[z];
			int height = tileHeights[var12][var14] + tileHeights[var11][var14] + tileHeights[var12][var13] + tileHeights[var11][var13] >> 2;
			int xSize = (x << 7) + (width << 6);
			int ySize = (y << 7) + (length << 6);


			long tag = IsaacCipher.calculateTag(x, y, 2, objectDefinition.int1 == 0, type);
			int flags = type + (orientation << 6);
			if (objectDefinition.int3 == 1) {
				flags += 256;
			}

			int var23;
			int var24;
			if (objectDefinition.hasSound()) {
				ObjectSound var22 = new ObjectSound();
				var22.plane = z;
				var22.x = x * 128;
				var22.y = y * 128;
				var23 = objectDefinition.sizeX;
				var24 = objectDefinition.sizeY;
				if (orientation == 1 || orientation == 3) {
					var23 = objectDefinition.sizeY;
					var24 = objectDefinition.sizeX;
				}

				var22.field1110 = (var23 + x) * 128;
				var22.field1100 = (var24 + y) * 128;
				var22.soundEffectId = objectDefinition.ambientSoundId;
				var22.field1106 = objectDefinition.int4 * 128;
				var22.field1104 = objectDefinition.int5;
				var22.field1105 = objectDefinition.int6;
				var22.soundEffectIds = objectDefinition.soundEffectIds;
				if (objectDefinition.transforms != null) {
					var22.obj = objectDefinition;
					var22.set();
				}

				ObjectSound.objectSounds.addFirst(var22);
				if (var22.soundEffectIds != null) {
					var22.field1107 = var22.field1104 + (int)(Math.random() * (double)(var22.field1105 - var22.field1104));
				}
			}

			Object renderable;
			if (type == 22) {
				if (!Client.isLowDetail || objectDefinition.int1 != 0 || objectDefinition.interactType == 1 || objectDefinition.boolean2) {
					if (objectDefinition.animationId == -1 && objectDefinition.transforms == null) {
						renderable = objectDefinition.getRenderable(22, orientation, tileHeights, xSize, height, ySize);
					} else {
						renderable = new DynamicObject(type, 22, orientation, z, x, y, objectDefinition.animationId, true, (Renderable)null);
					}

					scene.newFloorDecoration(z, x, y, height, (Renderable)renderable, tag, flags);
					if (objectDefinition.interactType == 1 && collisionMap != null) {
						collisionMap.setBlockedByFloorDec(x, y);
					}

				}
			}
			else if (type != 10 && type != 11) {
				int[] var10000;
				if (type >= 12) {
					if (objectDefinition.animationId == -1 && objectDefinition.transforms == null) {
						renderable = objectDefinition.getRenderable(type, orientation, tileHeights, xSize, height, ySize);
					} else {
						renderable = new DynamicObject(type, type, orientation, z, x, y, objectDefinition.animationId, true, (Renderable)null);
					}

					scene.newGameOjbect(z, x, y, height, 1, 1, (Renderable)renderable, 0, tag, flags);
					if (type >= 12 && type <= 17 && type != 13 && z > 0) {
						var10000 = class51.field404[z][x];
						var10000[y] |= 2340;
					}

					if (objectDefinition.interactType != 0 && collisionMap != null) {
						collisionMap.addGameObject(x, y, width, length, objectDefinition.boolean1);
					}

				} else if (type == 0) {
					// pretty sure this is all walls in here
					if (objectDefinition.animationId == -1 && objectDefinition.transforms == null) {
						renderable = objectDefinition.getRenderable(0, orientation, tileHeights, xSize, height, ySize);
					} else {
						renderable = new DynamicObject(type, 0, orientation, z, x, y, objectDefinition.animationId, true, (Renderable)null);
					}

					scene.newBoundaryObject(z, x, y, height, (Renderable)renderable, (Renderable)null, SceneRegion.field544[orientation], 0, tag, flags);
					if (orientation == 0) {
						if (objectDefinition.clipped) {
							SoundCache.field1462[z][x][y] = 50;
							SoundCache.field1462[z][x][y + 1] = 50;
						}

						if (objectDefinition.modelClipped) {
							var10000 = class51.field404[z][x];
							var10000[y] |= 585;
						}
					} else if (orientation == 1) {
						if (objectDefinition.clipped) {
							SoundCache.field1462[z][x][y + 1] = 50;
							SoundCache.field1462[z][x + 1][y + 1] = 50;
						}

						if (objectDefinition.modelClipped) {
							var10000 = class51.field404[z][x];
							var10000[y + 1] |= 1170;
						}
					} else if (orientation == 2) {
						if (objectDefinition.clipped) {
							SoundCache.field1462[z][x + 1][y] = 50;
							SoundCache.field1462[z][x + 1][y + 1] = 50;
						}

						if (objectDefinition.modelClipped) {
							var10000 = class51.field404[z][x + 1];
							var10000[y] |= 585;
						}
					} else if (orientation == 3) {
						if (objectDefinition.clipped) {
							SoundCache.field1462[z][x][y] = 50;
							SoundCache.field1462[z][x + 1][y] = 50;
						}

						if (objectDefinition.modelClipped) {
							var10000 = class51.field404[z][x];
							var10000[y] |= 1170;
						}
					}

					if (objectDefinition.interactType != 0 && collisionMap != null) {
						collisionMap.method3630(x, y, type, orientation, objectDefinition.boolean1);
					}

					if (objectDefinition.int2 != 16) {
						scene.method3210(z, x, y, objectDefinition.int2);
					}

				} else if (type == 1) {
					if (objectDefinition.animationId == -1 && objectDefinition.transforms == null) {
						renderable = objectDefinition.getRenderable(1, orientation, tileHeights, xSize, height, ySize);
					} else {
						renderable = new DynamicObject(type, 1, orientation, z, x, y, objectDefinition.animationId, true, (Renderable)null);
					}

					scene.newBoundaryObject(z, x, y, height, (Renderable)renderable, (Renderable)null, SceneRegion.field542[orientation], 0, tag, flags);
					if (objectDefinition.clipped) {
						if (orientation == 0) {
							SoundCache.field1462[z][x][y + 1] = 50;
						} else if (orientation == 1) {
							SoundCache.field1462[z][x + 1][y + 1] = 50;
						} else if (orientation == 2) {
							SoundCache.field1462[z][x + 1][y] = 50;
						} else if (orientation == 3) {
							SoundCache.field1462[z][x][y] = 50;
						}
					}

					if (objectDefinition.interactType != 0 && collisionMap != null) {
						collisionMap.method3630(x, y, type, orientation, objectDefinition.boolean1);
					}

				} else {
					int var28;
					if (type == 2) {
						var28 = orientation + 1 & 3;
						Object var29;
						Object var30;
						if (objectDefinition.animationId == -1 && objectDefinition.transforms == null) {
							var29 = objectDefinition.getRenderable(2, orientation + 4, tileHeights, xSize, height, ySize);
							var30 = objectDefinition.getRenderable(2, var28, tileHeights, xSize, height, ySize);
						} else {
							var29 = new DynamicObject(type, 2, orientation + 4, z, x, y, objectDefinition.animationId, true, (Renderable)null);
							var30 = new DynamicObject(type, 2, var28, z, x, y, objectDefinition.animationId, true, (Renderable)null);
						}

						scene.newBoundaryObject(z, x, y, height, (Renderable)var29, (Renderable)var30, SceneRegion.field544[orientation], SceneRegion.field544[var28], tag, flags);
						if (objectDefinition.modelClipped) {
							if (orientation == 0) {
								var10000 = class51.field404[z][x];
								var10000[y] |= 585;
								var10000 = class51.field404[z][x];
								var10000[1 + y] |= 1170;
							} else if (orientation == 1) {
								var10000 = class51.field404[z][x];
								var10000[y + 1] |= 1170;
								var10000 = class51.field404[z][x + 1];
								var10000[y] |= 585;
							} else if (orientation == 2) {
								var10000 = class51.field404[z][x + 1];
								var10000[y] |= 585;
								var10000 = class51.field404[z][x];
								var10000[y] |= 1170;
							} else if (orientation == 3) {
								var10000 = class51.field404[z][x];
								var10000[y] |= 1170;
								var10000 = class51.field404[z][x];
								var10000[y] |= 585;
							}
						}

						if (objectDefinition.interactType != 0 && collisionMap != null) {
							collisionMap.method3630(x, y, type, orientation, objectDefinition.boolean1);
						}

						if (objectDefinition.int2 != 16) {
							scene.method3210(z, x, y, objectDefinition.int2);
						}

					} else if (type == 3) {
						if (objectDefinition.animationId == -1 && objectDefinition.transforms == null) {
							renderable = objectDefinition.getRenderable(type, orientation, tileHeights, xSize, height, ySize);
						} else {
							renderable = new DynamicObject(type, type, orientation, z, x, y, objectDefinition.animationId, true, null);
						}

						scene.newBoundaryObject(z, x, y, height, (Renderable)renderable, (Renderable)null, SceneRegion.field542[orientation], 0, tag, flags);
						if (objectDefinition.clipped) {
							if (orientation == 0) {
								SoundCache.field1462[z][x][y + 1] = 50;
							} else if (orientation == 1) {
								SoundCache.field1462[z][x + 1][y + 1] = 50;
							} else if (orientation == 2) {
								SoundCache.field1462[z][x + 1][y] = 50;
							} else if (orientation == 3) {
								SoundCache.field1462[z][x][y] = 50;
							}
						}

						if (objectDefinition.interactType != 0 && collisionMap != null) {
							collisionMap.method3630(x, y, type, orientation, objectDefinition.boolean1);
						}

					} else if (type == 9) {
						// diagonal wall
						if (objectDefinition.animationId == -1 && objectDefinition.transforms == null) {
							renderable = objectDefinition.getRenderable(type, orientation, tileHeights, xSize, height, ySize);
						} else {
							renderable = new DynamicObject(type, type, orientation, z, x, y, objectDefinition.animationId, true, (Renderable)null);
						}

						scene.newGameOjbect(z, x, y, height, 1, 1, (Renderable)renderable, 0, tag, flags);
						if (objectDefinition.interactType != 0 && collisionMap != null) {
							collisionMap.addGameObject(x, y, width, length, objectDefinition.boolean1);
						}

						if (objectDefinition.int2 != 16) {
							scene.method3210(z, x, y, objectDefinition.int2);
						}

					} else if (type == 4) {
						if (objectDefinition.animationId == -1 && objectDefinition.transforms == null) {
							renderable = objectDefinition.getRenderable(4, orientation, tileHeights, xSize, height, ySize);
						} else {
							renderable = new DynamicObject(var5, 4, orientation, z, x, y, objectDefinition.animationId, true, (Renderable)null);
						}

						scene.newWallDecoration(z, x, y, height, (Renderable)renderable, (Renderable)null, SceneRegion.field544[orientation], 0, 0, 0, tag, flags);
					} else {
						// special wall decorations
						long var31;
						Object var33;
						if (type == 5) {
							var28 = 16;
							var31 = scene.getBoundaryObjectTag(z, x, y);
							if (var31 != 0L) {
								var28 = WorldMapSection2.getObjectDefinition(WorldMapRectangle.Entity_unpackID(var31)).int2;
							}

							if (objectDefinition.animationId == -1 && objectDefinition.transforms == null) {
								var33 = objectDefinition.getRenderable(4, orientation, tileHeights, xSize, height, ySize);
							} else {
								var33 = new DynamicObject(type, 4, orientation, z, x, y, objectDefinition.animationId, true, (Renderable)null);
							}

							scene.newWallDecoration(z, x, y, height, (Renderable)var33, (Renderable)null, SceneRegion.field544[orientation], 0, var28 * SceneRegion.field541[orientation], var28 * SceneRegion.field547[orientation], tag, flags);
						} else if (type == 6) {
							var28 = 8;
							var31 = scene.getBoundaryObjectTag(z, x, y);
							if (0L != var31) {
								var28 = WorldMapSection2.getObjectDefinition(WorldMapRectangle.Entity_unpackID(var31)).int2 / 2;
							}

							if (objectDefinition.animationId == -1 && objectDefinition.transforms == null) {
								var33 = objectDefinition.getRenderable(4, orientation + 4, tileHeights, xSize, height, ySize);
							} else {
								var33 = new DynamicObject(type, 4, orientation + 4, z, x, y, objectDefinition.animationId, true, (Renderable)null);
							}

							scene.newWallDecoration(z, x, y, height, (Renderable)var33, (Renderable)null, 256, orientation, var28 * SceneRegion.field546[orientation], var28 * SceneRegion.field552[orientation], tag, flags);
						} else if (type == 7) {
							var23 = orientation + 2 & 3;
							if (objectDefinition.animationId == -1 && objectDefinition.transforms == null) {
								renderable = objectDefinition.getRenderable(4, var23 + 4, tileHeights, xSize, height, ySize);
							} else {
								renderable = new DynamicObject(type, 4, var23 + 4, z, x, y, objectDefinition.animationId, true, (Renderable)null);
							}

							scene.newWallDecoration(z, x, y, height, (Renderable)renderable, (Renderable)null, 256, var23, 0, 0, tag, flags);
						} else if (type == 8) {
							var28 = 8;
							var31 = scene.getBoundaryObjectTag(z, x, y);
							if (var31 != 0L) {
								var28 = WorldMapSection2.getObjectDefinition(WorldMapRectangle.Entity_unpackID(var31)).int2 / 2;
							}

							int var27 = orientation + 2 & 3;
							Object var26;
							if (objectDefinition.animationId == -1 && objectDefinition.transforms == null) {
								var33 = objectDefinition.getRenderable(4, orientation + 4, tileHeights, xSize, height, ySize);
								var26 = objectDefinition.getRenderable(4, var27 + 4, tileHeights, xSize, height, ySize);
							} else {
								var33 = new DynamicObject(type, 4, orientation + 4, z, x, y, objectDefinition.animationId, true, (Renderable)null);
								var26 = new DynamicObject(type, 4, var27 + 4, z, x, y, objectDefinition.animationId, true, (Renderable)null);
							}

							scene.newWallDecoration(z, x, y, height, (Renderable)var33, (Renderable)var26, 256, orientation, var28 * SceneRegion.field546[orientation], var28 * SceneRegion.field552[orientation], tag, flags);
						}
					}
				}
			} else {
				if (objectDefinition.animationId == -1 && objectDefinition.transforms == null) {
					renderable = objectDefinition.getRenderable(10, orientation, tileHeights, xSize, height, ySize);
				} else {
					renderable = new DynamicObject(type, 10, orientation, z, x, y, objectDefinition.animationId, true, (Renderable)null);
				}

				if (renderable != null && scene.newGameOjbect(z, x, y, height, width, length, (Renderable)renderable, type == 11 ? 256 : 0, tag, flags) && objectDefinition.clipped) {
					var23 = 15;
					if (renderable instanceof Model) {
						var23 = ((Model)renderable).method2993() / 4;
						if (var23 > 30) {
							var23 = 30;
						}
					}

					for (var24 = 0; var24 <= width; ++var24) {
						for (int var25 = 0; var25 <= length; ++var25) {
							if (var23 > SoundCache.field1462[z][var24 + x][var25 + y]) {
								SoundCache.field1462[z][var24 + x][var25 + y] = (byte)var23;
							}
						}
					}
				}

				if (objectDefinition.interactType != 0 && collisionMap != null) {
					collisionMap.addGameObject(x, y, width, length, objectDefinition.boolean1);
				}

			}
		}
	}
}

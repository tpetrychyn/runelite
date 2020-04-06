import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import net.runelite.mapping.Export;
import net.runelite.mapping.Implements;
import net.runelite.mapping.ObfuscatedName;
import net.runelite.mapping.ObfuscatedSignature;
import net.runelite.rs.ScriptOpcodes;

@ObfuscatedName("fz")
@Implements("UserComparator6")
public class UserComparator6 extends AbstractUserComparator {
	@ObfuscatedName("er")
	@ObfuscatedSignature(
		signature = "Llp;"
	)
	@Export("spriteIds")
	static GraphicsDefaults spriteIds;
	@ObfuscatedName("q")
	@Export("reversed")
	final boolean reversed;

	public UserComparator6(boolean var1) {
		this.reversed = var1;
	}

	@ObfuscatedName("q")
	@ObfuscatedSignature(
		signature = "(Ljz;Ljz;I)I",
		garbageValue = "660153035"
	)
	@Export("compareBuddy")
	int compareBuddy(Buddy var1, Buddy var2) {
		if (var1.world != 0 && var2.world != 0) {
			return this.reversed ? var1.getUsername().compareToTyped(var2.getUsername()) : var2.getUsername().compareToTyped(var1.getUsername());
		} else {
			return this.compareUser(var1, var2);
		}
	}

	public int compare(Object var1, Object var2) {
		return this.compareBuddy((Buddy)var1, (Buddy)var2);
	}

	@ObfuscatedName("q")
	@ObfuscatedSignature(
		signature = "(Ljava/lang/String;Ljava/lang/String;III)V",
		garbageValue = "491873255"
	)
	public static void method3440(String var0, String var1, int var2, int var3) throws IOException {
		class40.idxCount = var3;
		TextureProvider.cacheGamebuild = var2;

		try {
			ScriptEvent.field586 = System.getProperty("os.name");
		} catch (Exception var14) {
			ScriptEvent.field586 = "Unknown";
		}

		GrandExchangeOfferOwnWorldComparator.field647 = ScriptEvent.field586.toLowerCase();

		try {
			PlayerType.userHomeDirectory = System.getProperty("user.home");
			if (PlayerType.userHomeDirectory != null) {
				PlayerType.userHomeDirectory = PlayerType.userHomeDirectory + "/";
			}
		} catch (Exception var13) {
		}

		try {
			if (GrandExchangeOfferOwnWorldComparator.field647.startsWith("win")) {
				if (PlayerType.userHomeDirectory == null) {
					PlayerType.userHomeDirectory = System.getenv("USERPROFILE");
				}
			} else if (PlayerType.userHomeDirectory == null) {
				PlayerType.userHomeDirectory = System.getenv("HOME");
			}

			if (PlayerType.userHomeDirectory != null) {
				PlayerType.userHomeDirectory = PlayerType.userHomeDirectory + "/";
			}
		} catch (Exception var12) {
		}

		if (PlayerType.userHomeDirectory == null) {
			PlayerType.userHomeDirectory = "~/";
		}

		JagexCache.field2043 = new String[]{"c:/rscache/", "/rscache/", "c:/windows/", "c:/winnt/", "c:/", PlayerType.userHomeDirectory, "/tmp/", ""};
		LoginPacket.field2309 = new String[]{".jagex_cache_" + TextureProvider.cacheGamebuild, ".file_store_" + TextureProvider.cacheGamebuild};
		int var9 = 0;

		int var7;
		File var8;
		label135:
		while (var9 < 4) {
			class1.cacheDir = TextureProvider.method2760(var0, var1, var9);
			if (!class1.cacheDir.exists()) {
				class1.cacheDir.mkdirs();
			}

			File[] var5 = class1.cacheDir.listFiles();
			if (var5 == null) {
				break;
			}

			File[] var6 = var5;
			var7 = 0;

			while (true) {
				if (var7 >= var6.length) {
					break label135;
				}

				var8 = var6[var7];
				if (!class186.method3618(var8, false)) {
					++var9;
					break;
				}

				++var7;
			}
		}

		File var4 = class1.cacheDir;
		FileSystem.FileSystem_cacheDir = var4;
		if (!FileSystem.FileSystem_cacheDir.exists()) {
			throw new RuntimeException("");
		} else {
			FileSystem.FileSystem_hasPermissions = true;

			try {
				File var16 = new File(PlayerType.userHomeDirectory, "random.dat");
				if (var16.exists()) {
					JagexCache.JagexCache_randomDat = new BufferedFile(new AccessFile(var16, "rw", 25L), 24, 0);
				} else {
					label115:
					for (int var10 = 0; var10 < LoginPacket.field2309.length; ++var10) {
						for (var7 = 0; var7 < JagexCache.field2043.length; ++var7) {
							var8 = new File(JagexCache.field2043[var7] + LoginPacket.field2309[var10] + File.separatorChar + "random.dat");
							if (var8.exists()) {
								JagexCache.JagexCache_randomDat = new BufferedFile(new AccessFile(var8, "rw", 25L), 24, 0);
								break label115;
							}
						}
					}
				}

				if (JagexCache.JagexCache_randomDat == null) {
					RandomAccessFile var17 = new RandomAccessFile(var16, "rw");
					var7 = var17.read();
					var17.seek(0L);
					var17.write(var7);
					var17.seek(0L);
					var17.close();
					JagexCache.JagexCache_randomDat = new BufferedFile(new AccessFile(var16, "rw", 25L), 24, 0);
				}
			} catch (IOException var15) {
			}

			JagexCache.JagexCache_dat2File = new BufferedFile(new AccessFile(class65.getFile("main_file_cache.dat2"), "rw", 1048576000L), 5200, 0);
			JagexCache.JagexCache_idx255File = new BufferedFile(new AccessFile(class65.getFile("main_file_cache.idx255"), "rw", 1048576L), 6000, 0);
			JagexCache.JagexCache_idxFiles = new BufferedFile[class40.idxCount];

			for (int var11 = 0; var11 < class40.idxCount; ++var11) {
				JagexCache.JagexCache_idxFiles[var11] = new BufferedFile(new AccessFile(class65.getFile("main_file_cache.idx" + var11), "rw", 1048576L), 6000, 0);
			}

		}
	}

	@ObfuscatedName("ao")
	@ObfuscatedSignature(
		signature = "(ILcx;ZI)I",
		garbageValue = "1163617683"
	)
	static int method3436(int var0, Script var1, boolean var2) {
		if (var0 == ScriptOpcodes.GETWINDOWMODE) {
			Interpreter.Interpreter_intStack[++HealthBarUpdate.Interpreter_intStackSize - 1] = AbstractWorldMapIcon.getWindowedMode();
			return 1;
		} else {
			int var3;
			if (var0 == ScriptOpcodes.SETWINDOWMODE) {
				var3 = Interpreter.Interpreter_intStack[--HealthBarUpdate.Interpreter_intStackSize];
				if (var3 == 1 || var3 == 2) {
					Clock.setWindowedMode(var3);
				}

				return 1;
			} else if (var0 == ScriptOpcodes.GETDEFAULTWINDOWMODE) {
				Interpreter.Interpreter_intStack[++HealthBarUpdate.Interpreter_intStackSize - 1] = WorldMapLabelSize.clientPreferences.windowMode;
				return 1;
			} else if (var0 != ScriptOpcodes.SETDEFAULTWINDOWMODE) {
				if (var0 == 5310) {
					--HealthBarUpdate.Interpreter_intStackSize;
					return 1;
				} else {
					return 2;
				}
			} else {
				var3 = Interpreter.Interpreter_intStack[--HealthBarUpdate.Interpreter_intStackSize];
				if (var3 == 1 || var3 == 2) {
					WorldMapLabelSize.clientPreferences.windowMode = var3;
					ReflectionCheck.savePreferences();
				}

				return 1;
			}
		}
	}
}

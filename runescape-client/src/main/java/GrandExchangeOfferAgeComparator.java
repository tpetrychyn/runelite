import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Comparator;
import net.runelite.mapping.Export;
import net.runelite.mapping.Implements;
import net.runelite.mapping.ObfuscatedGetter;
import net.runelite.mapping.ObfuscatedName;
import net.runelite.mapping.ObfuscatedSignature;

@ObfuscatedName("o")
@Implements("GrandExchangeOfferAgeComparator")
final class GrandExchangeOfferAgeComparator implements Comparator {
	@ObfuscatedName("sg")
	@ObfuscatedSignature(
		signature = "Lml;"
	)
	@Export("worldMap")
	static WorldMap worldMap;
	@ObfuscatedName("e")
	@Export("Tiles_hue")
	static int[] Tiles_hue;
	@ObfuscatedName("ee")
	@ObfuscatedGetter(
		intValue = 1640375913
	)
	@Export("port2")
	static int port2;

	@ObfuscatedName("x")
	@ObfuscatedSignature(
		signature = "(Li;Li;I)I",
		garbageValue = "-720251669"
	)
	@Export("compare_bridged")
	int compare_bridged(GrandExchangeEvent var1, GrandExchangeEvent var2) {
		return var1.age < var2.age ? -1 : (var2.age == var1.age ? 0 : 1);
	}

	public int compare(Object var1, Object var2) {
		return this.compare_bridged((GrandExchangeEvent)var1, (GrandExchangeEvent)var2);
	}

	public boolean equals(Object var1) {
		return super.equals(var1);
	}

	@ObfuscatedName("q")
	@ObfuscatedSignature(
		signature = "(Lbr;I)V",
		garbageValue = "1934352071"
	)
	@Export("doCycleTitle")
	static void doCycleTitle(GameShell var0) {
		int var4;
		if (Login.worldSelectOpen) {
			while (true) {
				if (!class22.isKeyDown()) {
					if (MouseHandler.MouseHandler_lastButton != 1 && (class217.mouseCam || MouseHandler.MouseHandler_lastButton != 4)) {
						break;
					}

					int var1 = Login.xPadding + 280;
					if (MouseHandler.MouseHandler_lastPressedX >= var1 && MouseHandler.MouseHandler_lastPressedX <= var1 + 14 && MouseHandler.MouseHandler_lastPressedY >= 4 && MouseHandler.MouseHandler_lastPressedY <= 18) {
						WorldMapSectionType.changeWorldSelectSorting(0, 0);
						break;
					}

					if (MouseHandler.MouseHandler_lastPressedX >= var1 + 15 && MouseHandler.MouseHandler_lastPressedX <= var1 + 80 && MouseHandler.MouseHandler_lastPressedY >= 4 && MouseHandler.MouseHandler_lastPressedY <= 18) {
						WorldMapSectionType.changeWorldSelectSorting(0, 1);
						break;
					}

					int var2 = Login.xPadding + 390;
					if (MouseHandler.MouseHandler_lastPressedX >= var2 && MouseHandler.MouseHandler_lastPressedX <= var2 + 14 && MouseHandler.MouseHandler_lastPressedY >= 4 && MouseHandler.MouseHandler_lastPressedY <= 18) {
						WorldMapSectionType.changeWorldSelectSorting(1, 0);
						break;
					}

					if (MouseHandler.MouseHandler_lastPressedX >= var2 + 15 && MouseHandler.MouseHandler_lastPressedX <= var2 + 80 && MouseHandler.MouseHandler_lastPressedY >= 4 && MouseHandler.MouseHandler_lastPressedY <= 18) {
						WorldMapSectionType.changeWorldSelectSorting(1, 1);
						break;
					}

					int var32 = Login.xPadding + 500;
					if (MouseHandler.MouseHandler_lastPressedX >= var32 && MouseHandler.MouseHandler_lastPressedX <= var32 + 14 && MouseHandler.MouseHandler_lastPressedY >= 4 && MouseHandler.MouseHandler_lastPressedY <= 18) {
						WorldMapSectionType.changeWorldSelectSorting(2, 0);
						break;
					}

					if (MouseHandler.MouseHandler_lastPressedX >= var32 + 15 && MouseHandler.MouseHandler_lastPressedX <= var32 + 80 && MouseHandler.MouseHandler_lastPressedY >= 4 && MouseHandler.MouseHandler_lastPressedY <= 18) {
						WorldMapSectionType.changeWorldSelectSorting(2, 1);
						break;
					}

					var4 = Login.xPadding + 610;
					if (MouseHandler.MouseHandler_lastPressedX >= var4 && MouseHandler.MouseHandler_lastPressedX <= var4 + 14 && MouseHandler.MouseHandler_lastPressedY >= 4 && MouseHandler.MouseHandler_lastPressedY <= 18) {
						WorldMapSectionType.changeWorldSelectSorting(3, 0);
						break;
					}

					if (MouseHandler.MouseHandler_lastPressedX >= var4 + 15 && MouseHandler.MouseHandler_lastPressedX <= var4 + 80 && MouseHandler.MouseHandler_lastPressedY >= 4 && MouseHandler.MouseHandler_lastPressedY <= 18) {
						WorldMapSectionType.changeWorldSelectSorting(3, 1);
						break;
					}

					if (MouseHandler.MouseHandler_lastPressedX >= Login.xPadding + 708 && MouseHandler.MouseHandler_lastPressedY >= 4 && MouseHandler.MouseHandler_lastPressedX <= Login.xPadding + 708 + 50 && MouseHandler.MouseHandler_lastPressedY <= 20) {
						Login.worldSelectOpen = false;
						Login.leftTitleSprite.drawAt(Login.xPadding, 0);
						DirectByteArrayCopier.rightTitleSprite.drawAt(Login.xPadding + 382, 0);
						class3.logoSprite.drawAt(Login.xPadding + 382 - class3.logoSprite.subWidth / 2, 18);
						break;
					}

					if (Login.hoveredWorldIndex != -1) {
						World var5 = UserComparator2.World_worlds[Login.hoveredWorldIndex];
						UserComparator3.changeWorld(var5);
						Login.worldSelectOpen = false;
						Login.leftTitleSprite.drawAt(Login.xPadding, 0);
						DirectByteArrayCopier.rightTitleSprite.drawAt(Login.xPadding + 382, 0);
						class3.logoSprite.drawAt(Login.xPadding + 382 - class3.logoSprite.subWidth / 2, 18);
					} else {
						if (Login.worldSelectPage > 0 && class92.worldSelectLeftSprite != null && MouseHandler.MouseHandler_lastPressedX >= 0 && MouseHandler.MouseHandler_lastPressedX <= class92.worldSelectLeftSprite.subWidth && MouseHandler.MouseHandler_lastPressedY >= Varps.canvasHeight / 2 - 50 && MouseHandler.MouseHandler_lastPressedY <= Varps.canvasHeight / 2 + 50) {
							--Login.worldSelectPage;
						}

						if (Login.worldSelectPage < Login.worldSelectPagesCount && StructDefinition.worldSelectRightSprite != null && MouseHandler.MouseHandler_lastPressedX >= WorldMapLabel.canvasWidth - StructDefinition.worldSelectRightSprite.subWidth - 5 && MouseHandler.MouseHandler_lastPressedX <= WorldMapLabel.canvasWidth && MouseHandler.MouseHandler_lastPressedY >= Varps.canvasHeight / 2 - 50 && MouseHandler.MouseHandler_lastPressedY <= Varps.canvasHeight / 2 + 50) {
							++Login.worldSelectPage;
						}
					}
					break;
				}

				if (class3.field16 == 13) {
					HealthBar.method2124();
					break;
				}

				if (class3.field16 == 96) {
					if (Login.worldSelectPage > 0 && class92.worldSelectLeftSprite != null) {
						--Login.worldSelectPage;
					}
				} else if (class3.field16 == 97 && Login.worldSelectPage < Login.worldSelectPagesCount && StructDefinition.worldSelectRightSprite != null) {
					++Login.worldSelectPage;
				}
			}

		} else {
			if ((MouseHandler.MouseHandler_lastButton == 1 || !class217.mouseCam && MouseHandler.MouseHandler_lastButton == 4) && MouseHandler.MouseHandler_lastPressedX >= Login.xPadding + 765 - 50 && MouseHandler.MouseHandler_lastPressedY >= 453) {
				GrandExchangeOfferOwnWorldComparator.clientPreferences.titleMusicDisabled = !GrandExchangeOfferOwnWorldComparator.clientPreferences.titleMusicDisabled;
				WorldMapArea.savePreferences();
				if (!GrandExchangeOfferOwnWorldComparator.clientPreferences.titleMusicDisabled) {
					class105.method2394(AbstractWorldMapIcon.archive6, "scape main", "", 255, false);
				} else {
					Client.method1496();
				}
			}

			if (Client.gameState != 5) {
				if (Login.field1223 == -1L) {
					Login.field1223 = class217.currentTimeMillis() + 1000L;
				}

				long var21 = class217.currentTimeMillis();
				boolean var3;
				if (Client.archiveLoaders != null && Client.archiveLoadersDone < Client.archiveLoaders.size()) {
					while (true) {
						if (Client.archiveLoadersDone >= Client.archiveLoaders.size()) {
							var3 = true;
							break;
						}

						ArchiveLoader var23 = (ArchiveLoader)Client.archiveLoaders.get(Client.archiveLoadersDone);
						if (!var23.isLoaded()) {
							var3 = false;
							break;
						}

						++Client.archiveLoadersDone;
					}
				} else {
					var3 = true;
				}

				if (var3 && -1L == Login.field1224) {
					Login.field1224 = var21;
					if (Login.field1224 > Login.field1223) {
						Login.field1223 = Login.field1224;
					}
				}

				if (Client.gameState == 10 || Client.gameState == 11) {
					if (IgnoreList.clientLanguage == Language.Language_EN) {
						if (MouseHandler.MouseHandler_lastButton == 1 || !class217.mouseCam && MouseHandler.MouseHandler_lastButton == 4) {
							var4 = Login.xPadding + 5;
							short var24 = 463;
							byte var6 = 100;
							byte var7 = 35;
							if (MouseHandler.MouseHandler_lastPressedX >= var4 && MouseHandler.MouseHandler_lastPressedX <= var6 + var4 && MouseHandler.MouseHandler_lastPressedY >= var24 && MouseHandler.MouseHandler_lastPressedY <= var7 + var24) {
								if (Language.loadWorlds()) {
									Login.worldSelectOpen = true;
									Login.worldSelectPage = 0;
									Login.worldSelectPagesCount = 0;
								}

								return;
							}
						}

						if (LoginPacket.World_request != null && Language.loadWorlds()) {
							Login.worldSelectOpen = true;
							Login.worldSelectPage = 0;
							Login.worldSelectPagesCount = 0;
						}
					}

					var4 = MouseHandler.MouseHandler_lastButton;
					int var46 = MouseHandler.MouseHandler_lastPressedX;
					int var33 = MouseHandler.MouseHandler_lastPressedY;
					if (var4 == 0) {
						var46 = MouseHandler.MouseHandler_x;
						var33 = MouseHandler.MouseHandler_y;
					}

					if (!class217.mouseCam && var4 == 4) {
						var4 = 1;
					}

					short var36;
					int var37;
					if (Login.loginIndex == 0) {
						boolean var44 = false;

						while (class22.isKeyDown()) {
							if (class3.field16 == 84) {
								var44 = true;
							}
						}

						var37 = PacketWriter.loginBoxCenter - 80;
						var36 = 291;
						if (var4 == 1 && var46 >= var37 - 75 && var46 <= var37 + 75 && var33 >= var36 - 20 && var33 <= var36 + 20) {
							Script.openURL(class41.method654("secure", true) + "m=account-creation/g=oldscape/create_account_funnel.ws", true, false);
						}

						var37 = PacketWriter.loginBoxCenter + 80;
						if (var4 == 1 && var46 >= var37 - 75 && var46 <= var37 + 75 && var33 >= var36 - 20 && var33 <= var36 + 20 || var44) {
							if ((Client.worldProperties & 33554432) != 0) {
								Login.Login_response0 = "";
								Login.Login_response1 = "This is a <col=00ffff>Beta<col=ffffff> world.";
								Login.Login_response2 = "Your normal account will not be affected.";
								Login.Login_response3 = "";
								Login.loginIndex = 1;
								FontName.method5388();
							} else if ((Client.worldProperties & 4) != 0) {
								if ((Client.worldProperties & 1024) != 0) {
									Login.Login_response1 = "This is a <col=ffff00>High Risk <col=ff0000>PvP<col=ffffff> world.";
									Login.Login_response2 = "Players can attack each other almost everywhere";
									Login.Login_response3 = "and the Protect Item prayer won't work.";
								} else {
									Login.Login_response1 = "This is a <col=ff0000>PvP<col=ffffff> world.";
									Login.Login_response2 = "Players can attack each other";
									Login.Login_response3 = "almost everywhere.";
								}

								Login.Login_response0 = "Warning!";
								Login.loginIndex = 1;
								FontName.method5388();
							} else if ((Client.worldProperties & 1024) != 0) {
								Login.Login_response1 = "This is a <col=ffff00>High Risk<col=ffffff> world.";
								Login.Login_response2 = "The Protect Item prayer will";
								Login.Login_response3 = "not work on this world.";
								Login.Login_response0 = "Warning!";
								Login.loginIndex = 1;
								FontName.method5388();
							} else {
								ObjectDefinition.Login_promptCredentials(false);
							}
						}
					} else {
						short var8;
						int var34;
						if (Login.loginIndex == 1) {
							while (true) {
								if (!class22.isKeyDown()) {
									var34 = PacketWriter.loginBoxCenter - 80;
									var8 = 321;
									if (var4 == 1 && var46 >= var34 - 75 && var46 <= var34 + 75 && var33 >= var8 - 20 && var33 <= var8 + 20) {
										ObjectDefinition.Login_promptCredentials(false);
									}

									var34 = PacketWriter.loginBoxCenter + 80;
									if (var4 == 1 && var46 >= var34 - 75 && var46 <= var34 + 75 && var33 >= var8 - 20 && var33 <= var8 + 20) {
										Login.loginIndex = 0;
									}
									break;
								}

								if (class3.field16 == 84) {
									ObjectDefinition.Login_promptCredentials(false);
								} else if (class3.field16 == 13) {
									Login.loginIndex = 0;
								}
							}
						} else {
							int var11;
							short var35;
							boolean var38;
							if (Login.loginIndex == 2) {
								var35 = 201;
								var34 = var35 + 52;
								if (var4 == 1 && var33 >= var34 - 12 && var33 < var34 + 2) {
									Login.currentLoginField = 0;
								}

								var34 += 15;
								if (var4 == 1 && var33 >= var34 - 12 && var33 < var34 + 2) {
									Login.currentLoginField = 1;
								}

								var34 += 15;
								var35 = 361;
								if (class41.field315 != null) {
									var37 = class41.field315.highX / 2;
									if (var4 == 1 && var46 >= class41.field315.lowX - var37 && var46 <= var37 + class41.field315.lowX && var33 >= var35 - 15 && var33 < var35) {
										switch(Login.field1218) {
										case 1:
											HealthBarUpdate.setLoginResponseString("Please enter your username.", "If you created your account after November", "2010, this will be the creation email address.");
											Login.loginIndex = 5;
											return;
										case 2:
											Script.openURL("https://support.runescape.com/hc/en-gb", true, false);
										}
									}
								}

								var37 = PacketWriter.loginBoxCenter - 80;
								var36 = 321;
								if (var4 == 1 && var46 >= var37 - 75 && var46 <= var37 + 75 && var33 >= var36 - 20 && var33 <= var36 + 20) {
									Login.Login_username = Login.Login_username.trim();
									if (Login.Login_username.length() == 0) {
										HealthBarUpdate.setLoginResponseString("", "Please enter your username/email address.", "");
										return;
									}

									if (Login.Login_password.length() == 0) {
										HealthBarUpdate.setLoginResponseString("", "Please enter your password.", "");
										return;
									}

									HealthBarUpdate.setLoginResponseString("", "Connecting to server...", "");
									GraphicsObject.method2122(false);
									MouseRecorder.updateGameState(20);
									return;
								}

								var37 = Login.loginBoxX + 180 + 80;
								if (var4 == 1 && var46 >= var37 - 75 && var46 <= var37 + 75 && var33 >= var36 - 20 && var33 <= var36 + 20) {
									Login.loginIndex = 0;
									Login.Login_username = "";
									Login.Login_password = "";
									GraphicsObject.field1141 = 0;
									MilliClock.otp = "";
									Login.field1214 = true;
								}

								var37 = PacketWriter.loginBoxCenter + -117;
								var36 = 277;
								Login.field1207 = var46 >= var37 && var46 < var37 + WorldMapLabelSize.field158 && var33 >= var36 && var33 < var36 + class219.field2515;
								if (var4 == 1 && Login.field1207) {
									Client.Login_isUsernameRemembered = !Client.Login_isUsernameRemembered;
									if (!Client.Login_isUsernameRemembered && GrandExchangeOfferOwnWorldComparator.clientPreferences.rememberedUsername != null) {
										GrandExchangeOfferOwnWorldComparator.clientPreferences.rememberedUsername = null;
										WorldMapArea.savePreferences();
									}
								}

								var37 = PacketWriter.loginBoxCenter + 24;
								var36 = 277;
								Login.field1212 = var46 >= var37 && var46 < var37 + WorldMapLabelSize.field158 && var33 >= var36 && var33 < var36 + class219.field2515;
								if (var4 == 1 && Login.field1212) {
									GrandExchangeOfferOwnWorldComparator.clientPreferences.hideUsername = !GrandExchangeOfferOwnWorldComparator.clientPreferences.hideUsername;
									if (!GrandExchangeOfferOwnWorldComparator.clientPreferences.hideUsername) {
										Login.Login_username = "";
										GrandExchangeOfferOwnWorldComparator.clientPreferences.rememberedUsername = null;
										FontName.method5388();
									}

									WorldMapArea.savePreferences();
								}

								while (true) {
									Transferable var26;
									int var42;
									do {
										while (true) {
											label1160:
											do {
												while (true) {
													while (class22.isKeyDown()) {
														if (class3.field16 != 13) {
															if (Login.currentLoginField != 0) {
																continue label1160;
															}

															char var39 = Coord.field2531;

															for (var11 = 0; var11 < "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!\"£$%^&*()-_=+[{]};:'@#~,<.>/?\\| ".length() && var39 != "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!\"£$%^&*()-_=+[{]};:'@#~,<.>/?\\| ".charAt(var11); ++var11) {
															}

															if (class3.field16 == 85 && Login.Login_username.length() > 0) {
																Login.Login_username = Login.Login_username.substring(0, Login.Login_username.length() - 1);
															}

															if (class3.field16 == 84 || class3.field16 == 80) {
																Login.currentLoginField = 1;
															}

															char var12 = Coord.field2531;
															boolean var40 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!\"£$%^&*()-_=+[{]};:'@#~,<.>/?\\| ".indexOf(var12) != -1;
															if (var40 && Login.Login_username.length() < 320) {
																Login.Login_username = Login.Login_username + Coord.field2531;
															}
														} else {
															Login.loginIndex = 0;
															Login.Login_username = "";
															Login.Login_password = "";
															GraphicsObject.field1141 = 0;
															MilliClock.otp = "";
															Login.field1214 = true;
														}
													}

													return;
												}
											} while(Login.currentLoginField != 1);

											if (class3.field16 == 85 && Login.Login_password.length() > 0) {
												Login.Login_password = Login.Login_password.substring(0, Login.Login_password.length() - 1);
											} else if (class3.field16 == 84 || class3.field16 == 80) {
												Login.currentLoginField = 0;
												if (class3.field16 == 84) {
													Login.Login_username = Login.Login_username.trim();
													if (Login.Login_username.length() == 0) {
														HealthBarUpdate.setLoginResponseString("", "Please enter your username/email address.", "");
														return;
													}

													if (Login.Login_password.length() == 0) {
														HealthBarUpdate.setLoginResponseString("", "Please enter your password.", "");
														return;
													}

													HealthBarUpdate.setLoginResponseString("", "Connecting to server...", "");
													GraphicsObject.method2122(false);
													MouseRecorder.updateGameState(20);
													return;
												}
											}

											if ((KeyHandler.KeyHandler_pressedKeys[82] || KeyHandler.KeyHandler_pressedKeys[87]) && class3.field16 == 67) {
												Clipboard var25 = Toolkit.getDefaultToolkit().getSystemClipboard();
												var26 = var25.getContents(WorldMapSection1.client);
												var42 = 20 - Login.Login_password.length();
												break;
											}

											char var43 = Coord.field2531;
											if ((var43 < ' ' || var43 >= 127) && (var43 <= 127 || var43 >= 160) && (var43 <= 160 || var43 > 255)) {
												label1367: {
													if (var43 != 0) {
														char[] var27 = class297.cp1252AsciiExtension;

														for (int var28 = 0; var28 < var27.length; ++var28) {
															char var14 = var27[var28];
															if (var43 == var14) {
																var38 = true;
																break label1367;
															}
														}
													}

													var38 = false;
												}
											} else {
												var38 = true;
											}

											if (var38) {
												char var47 = Coord.field2531;
												boolean var41 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!\"£$%^&*()-_=+[{]};:'@#~,<.>/?\\| ".indexOf(var47) != -1;
												if (var41 && Login.Login_password.length() < 20) {
													Login.Login_password = Login.Login_password + Coord.field2531;
												}
											}
										}
									} while(var42 <= 0);

									try {
										String var13 = (String)var26.getTransferData(DataFlavor.stringFlavor);
										int var45 = Math.min(var42, var13.length());

										for (int var15 = 0; var15 < var45; ++var15) {
											char var17 = var13.charAt(var15);
											boolean var16;
											if (var17 >= ' ' && var17 < 127 || var17 > 127 && var17 < 160 || var17 > 160 && var17 <= 255) {
												var16 = true;
											} else {
												label1381: {
													if (var17 != 0) {
														char[] var18 = class297.cp1252AsciiExtension;

														for (int var19 = 0; var19 < var18.length; ++var19) {
															char var20 = var18[var19];
															if (var20 == var17) {
																var16 = true;
																break label1381;
															}
														}
													}

													var16 = false;
												}
											}

											if (!var16 || !WorldMapSection2.method433(var13.charAt(var15))) {
												Login.loginIndex = 3;
												return;
											}
										}

										Login.Login_password = Login.Login_password + var13.substring(0, var45);
									} catch (UnsupportedFlavorException var30) {
									} catch (IOException var31) {
									}
								}
							} else if (Login.loginIndex == 3) {
								var34 = Login.loginBoxX + 180;
								var8 = 276;
								if (var4 == 1 && var46 >= var34 - 75 && var46 <= var34 + 75 && var33 >= var8 - 20 && var33 <= var8 + 20) {
									ObjectDefinition.Login_promptCredentials(false);
								}

								var34 = Login.loginBoxX + 180;
								var8 = 326;
								if (var4 == 1 && var46 >= var34 - 75 && var46 <= var34 + 75 && var33 >= var8 - 20 && var33 <= var8 + 20) {
									HealthBarUpdate.setLoginResponseString("Please enter your username.", "If you created your account after November", "2010, this will be the creation email address.");
									Login.loginIndex = 5;
									return;
								}
							} else {
								int var10;
								if (Login.loginIndex == 4) {
									var34 = Login.loginBoxX + 180 - 80;
									var8 = 321;
									if (var4 == 1 && var46 >= var34 - 75 && var46 <= var34 + 75 && var33 >= var8 - 20 && var33 <= var8 + 20) {
										MilliClock.otp.trim();
										if (MilliClock.otp.length() != 6) {
											HealthBarUpdate.setLoginResponseString("", "Please enter a 6-digit PIN.", "");
											return;
										}

										GraphicsObject.field1141 = Integer.parseInt(MilliClock.otp);
										MilliClock.otp = "";
										GraphicsObject.method2122(true);
										HealthBarUpdate.setLoginResponseString("", "Connecting to server...", "");
										MouseRecorder.updateGameState(20);
										return;
									}

									if (var4 == 1 && var46 >= Login.loginBoxX + 180 - 9 && var46 <= Login.loginBoxX + 180 + 130 && var33 >= 263 && var33 <= 296) {
										Login.field1214 = !Login.field1214;
									}

									if (var4 == 1 && var46 >= Login.loginBoxX + 180 - 34 && var46 <= Login.loginBoxX + 34 + 180 && var33 >= 351 && var33 <= 363) {
										Script.openURL(class41.method654("secure", true) + "m=totp-authenticator/disableTOTPRequest", true, false);
									}

									var34 = Login.loginBoxX + 180 + 80;
									if (var4 == 1 && var46 >= var34 - 75 && var46 <= var34 + 75 && var33 >= var8 - 20 && var33 <= var8 + 20) {
										Login.loginIndex = 0;
										Login.Login_username = "";
										Login.Login_password = "";
										GraphicsObject.field1141 = 0;
										MilliClock.otp = "";
									}

									while (class22.isKeyDown()) {
										boolean var9 = false;

										for (var10 = 0; var10 < "1234567890".length(); ++var10) {
											if (Coord.field2531 == "1234567890".charAt(var10)) {
												var9 = true;
												break;
											}
										}

										if (class3.field16 == 13) {
											Login.loginIndex = 0;
											Login.Login_username = "";
											Login.Login_password = "";
											GraphicsObject.field1141 = 0;
											MilliClock.otp = "";
										} else {
											if (class3.field16 == 85 && MilliClock.otp.length() > 0) {
												MilliClock.otp = MilliClock.otp.substring(0, MilliClock.otp.length() - 1);
											}

											if (class3.field16 == 84) {
												MilliClock.otp.trim();
												if (MilliClock.otp.length() != 6) {
													HealthBarUpdate.setLoginResponseString("", "Please enter a 6-digit PIN.", "");
													return;
												}

												GraphicsObject.field1141 = Integer.parseInt(MilliClock.otp);
												MilliClock.otp = "";
												GraphicsObject.method2122(true);
												HealthBarUpdate.setLoginResponseString("", "Connecting to server...", "");
												MouseRecorder.updateGameState(20);
												return;
											}

											if (var9 && MilliClock.otp.length() < 6) {
												MilliClock.otp = MilliClock.otp + Coord.field2531;
											}
										}
									}
								} else if (Login.loginIndex == 5) {
									var34 = Login.loginBoxX + 180 - 80;
									var8 = 321;
									if (var4 == 1 && var46 >= var34 - 75 && var46 <= var34 + 75 && var33 >= var8 - 20 && var33 <= var8 + 20) {
										WorldMapEvent.method864();
										return;
									}

									var34 = Login.loginBoxX + 180 + 80;
									if (var4 == 1 && var46 >= var34 - 75 && var46 <= var34 + 75 && var33 >= var8 - 20 && var33 <= var8 + 20) {
										ObjectDefinition.Login_promptCredentials(true);
									}

									var36 = 361;
									if (MusicPatchPcmStream.field2495 != null) {
										var10 = MusicPatchPcmStream.field2495.highX / 2;
										if (var4 == 1 && var46 >= MusicPatchPcmStream.field2495.lowX - var10 && var46 <= var10 + MusicPatchPcmStream.field2495.lowX && var33 >= var36 - 15 && var33 < var36) {
											Script.openURL(class41.method654("secure", true) + "m=weblogin/g=oldscape/cant_log_in", true, false);
										}
									}

									while (class22.isKeyDown()) {
										var38 = false;

										for (var11 = 0; var11 < "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!\"£$%^&*()-_=+[{]};:'@#~,<.>/?\\| ".length(); ++var11) {
											if (Coord.field2531 == "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!\"£$%^&*()-_=+[{]};:'@#~,<.>/?\\| ".charAt(var11)) {
												var38 = true;
												break;
											}
										}

										if (class3.field16 == 13) {
											ObjectDefinition.Login_promptCredentials(true);
										} else {
											if (class3.field16 == 85 && Login.Login_username.length() > 0) {
												Login.Login_username = Login.Login_username.substring(0, Login.Login_username.length() - 1);
											}

											if (class3.field16 == 84) {
												WorldMapEvent.method864();
												return;
											}

											if (var38 && Login.Login_username.length() < 320) {
												Login.Login_username = Login.Login_username + Coord.field2531;
											}
										}
									}
								} else if (Login.loginIndex != 6) {
									if (Login.loginIndex == 7) {
										var34 = Login.loginBoxX + 180 - 80;
										var8 = 321;
										if (var4 == 1 && var46 >= var34 - 75 && var46 <= var34 + 75 && var33 >= var8 - 20 && var33 <= var8 + 20) {
											Script.openURL(class41.method654("secure", true) + "m=dob/set_dob.ws", true, false);
											HealthBarUpdate.setLoginResponseString("", "Page has opened in a new window.", "(Please check your popup blocker.)");
											Login.loginIndex = 6;
											return;
										}

										var34 = Login.loginBoxX + 180 + 80;
										if (var4 == 1 && var46 >= var34 - 75 && var46 <= var34 + 75 && var33 >= var8 - 20 && var33 <= var8 + 20) {
											ObjectDefinition.Login_promptCredentials(true);
										}
									} else if (Login.loginIndex == 8) {
										var34 = Login.loginBoxX + 180 - 80;
										var8 = 321;
										if (var4 == 1 && var46 >= var34 - 75 && var46 <= var34 + 75 && var33 >= var8 - 20 && var33 <= var8 + 20) {
											Script.openURL("https://www.jagex.com/terms/privacy", true, false);
											HealthBarUpdate.setLoginResponseString("", "Page has opened in a new window.", "(Please check your popup blocker.)");
											Login.loginIndex = 6;
											return;
										}

										var34 = Login.loginBoxX + 180 + 80;
										if (var4 == 1 && var46 >= var34 - 75 && var46 <= var34 + 75 && var33 >= var8 - 20 && var33 <= var8 + 20) {
											ObjectDefinition.Login_promptCredentials(true);
										}
									} else if (Login.loginIndex == 12) {
										String var29 = "";
										switch(Login.field1201) {
										case 0:
											var29 = "https://support.runescape.com/hc/en-gb/articles/115002238729-Account-Bans";
											break;
										case 1:
											var29 = "https://support.runescape.com/hc/en-gb/articles/206103939-My-account-is-locked";
											break;
										default:
											ObjectDefinition.Login_promptCredentials(false);
										}

										var37 = Login.loginBoxX + 180;
										var36 = 276;
										if (var4 == 1 && var46 >= var37 - 75 && var46 <= var37 + 75 && var33 >= var36 - 20 && var33 <= var36 + 20) {
											Script.openURL(var29, true, false);
											HealthBarUpdate.setLoginResponseString("", "Page has opened in a new window.", "(Please check your popup blocker.)");
											Login.loginIndex = 6;
											return;
										}

										var37 = Login.loginBoxX + 180;
										var36 = 326;
										if (var4 == 1 && var46 >= var37 - 75 && var46 <= var37 + 75 && var33 >= var36 - 20 && var33 <= var36 + 20) {
											ObjectDefinition.Login_promptCredentials(false);
										}
									} else if (Login.loginIndex == 24) {
										var34 = Login.loginBoxX + 180;
										var8 = 301;
										if (var4 == 1 && var46 >= var34 - 75 && var46 <= var34 + 75 && var33 >= var8 - 20 && var33 <= var8 + 20) {
											ObjectDefinition.Login_promptCredentials(false);
										}
									}
								} else {
									while (true) {
										do {
											if (!class22.isKeyDown()) {
												var35 = 321;
												if (var4 == 1 && var33 >= var35 - 20 && var33 <= var35 + 20) {
													ObjectDefinition.Login_promptCredentials(true);
												}

												return;
											}
										} while(class3.field16 != 84 && class3.field16 != 13);

										ObjectDefinition.Login_promptCredentials(true);
									}
								}
							}
						}
					}

				}
			}
		}
	}

	@ObfuscatedName("q")
	@ObfuscatedSignature(
		signature = "(Lej;[Lfm;I)V",
		garbageValue = "-1662285695"
	)
	static final void method248(Scene scene, CollisionMap[] var1) {
		int z;
		int var3;
		int var4;
		int var5;
		for (z = 0; z < 4; ++z) {
			for (var3 = 0; var3 < 104; ++var3) {
				for (var4 = 0; var4 < 104; ++var4) {
					if ((SceneRegion.Tiles_renderFlags[z][var3][var4] & 1) == 1) {
						var5 = z;
						if ((SceneRegion.Tiles_renderFlags[1][var3][var4] & 2) == 2) {
							var5 = z - 1;
						}

						if (var5 >= 0) {
							var1[var5].setBlockedByFloor(var3, var4);
						}
					}
				}
			}
		}

		SceneRegion.field550 += (int)(Math.random() * 5.0D) - 2;
		if (SceneRegion.field550 < -8) {
			SceneRegion.field550 = -8;
		}

		if (SceneRegion.field550 > 8) {
			SceneRegion.field550 = 8;
		}

		SceneRegion.field548 += (int)(Math.random() * 5.0D) - 2;
		if (SceneRegion.field548 < -16) {
			SceneRegion.field548 = -16;
		}

		if (SceneRegion.field548 > 16) {
			SceneRegion.field548 = 16;
		}

		int var9;
		int var10;
		int var11;
		int var12;
		int var13;
		int var14;
		int var15;
		int var16;
		int[] var10000;
		int var17;
		int var18;
		for (z = 0; z < 4; ++z) {
			byte[][] var42 = SoundCache.field1462[z];
			var9 = (int)Math.sqrt(5100.0D);
			var10 = var9 * 768 >> 8;

			int var19;
			int var20;
			for (int y = 1; y < 103; ++y) {
				for (int x = 1; x < 103; ++x) {
					int xHeightDiff = SceneRegion.Tiles_heights[z][x + 1][y] - SceneRegion.Tiles_heights[z][x - 1][y];
					int yHeightDiff = SceneRegion.Tiles_heights[z][x][y + 1] - SceneRegion.Tiles_heights[z][x][y - 1];
					int diff = (int)Math.sqrt((double)(xHeightDiff * xHeightDiff + yHeightDiff * yHeightDiff + 65536));
					var16 = (xHeightDiff << 8) / diff;
					var17 = 65536 / diff;
					var18 = (yHeightDiff << 8) / diff;
					var19 = (var16 * -50 + var18 * -50 + var17 * -10) / var10 + 96;

					var20 = (var42[x - 1][y] >> 2) + (var42[x][y - 1] >> 2) + (var42[x + 1][y] >> 3) + (var42[x][y + 1] >> 3) + (var42[x][y] >> 1);
					SceneRegion.tileColors[x][y] = var19 - var20;
				}
			}

			for (var11 = 0; var11 < 104; ++var11) {
				Tiles_hue[var11] = 0;
				UserComparator6.Tiles_saturation[var11] = 0;
				StructDefinition.Tiles_lightness[var11] = 0;
				SceneRegion.Tiles_hueMultiplier[var11] = 0;
				FriendSystem.field1086[var11] = 0;
			}

			for (int xi = -5; xi < 109; ++xi) {
				for (int yi = 0; yi < 104; ++yi) {
					int xr = xi + 5;
					int var10002;
					if (xr >= 0 && xr < 104) {
						int underlayId = SceneRegion.underlayIds[z][xr][yi] & 255;
						if (underlayId > 0) {
							FloorUnderlayDefinition var43 = class60.method995(underlayId - 1);
							var10000 = Tiles_hue;
							var10000[yi] += var43.hue;
							var10000 = UserComparator6.Tiles_saturation;
							var10000[yi] += var43.saturation;
							var10000 = StructDefinition.Tiles_lightness;
							var10000[yi] += var43.lightness;
							var10000 = SceneRegion.Tiles_hueMultiplier;
							var10000[yi] += var43.hueMultiplier;
							var10002 = FriendSystem.field1086[yi]++;
						}
					}

					int xl = xi - 5;
					if (xl >= 0 && xl < 104) {
						int underlayId = SceneRegion.underlayIds[z][xl][yi] & 255;
						if (underlayId > 0) {
							FloorUnderlayDefinition var44 = class60.method995(underlayId - 1);
							var10000 = Tiles_hue;
							var10000[yi] -= var44.hue;
							var10000 = UserComparator6.Tiles_saturation;
							var10000[yi] -= var44.saturation;
							var10000 = StructDefinition.Tiles_lightness;
							var10000[yi] -= var44.lightness;
							var10000 = SceneRegion.Tiles_hueMultiplier;
							var10000[yi] -= var44.hueMultiplier;
							var10002 = FriendSystem.field1086[yi]--;
						}
					}
				}

				if (xi >= 1 && xi < 103) {
					int runningHues = 0;
					int runningSat = 0;
					int runningLight = 0;
					int runningMultiplier = 0;
					int runningNumber = 0;

					for (int yi = -5; yi < 109; ++yi) {
						int yu = yi + 5;
						if (yu >= 0 && yu < 104) {
							runningHues += Tiles_hue[yu];
							runningSat += UserComparator6.Tiles_saturation[yu];
							runningLight += StructDefinition.Tiles_lightness[yu];
							runningMultiplier += SceneRegion.Tiles_hueMultiplier[yu];
							runningNumber += FriendSystem.field1086[yu];
						}

						int yd = yi - 5;
						if (yd >= 0 && yd < 104) {
							runningHues -= Tiles_hue[yd];
							runningSat -= UserComparator6.Tiles_saturation[yd];
							runningLight -= StructDefinition.Tiles_lightness[yd];
							runningMultiplier -= SceneRegion.Tiles_hueMultiplier[yd];
							runningNumber -= FriendSystem.field1086[yd];
						}

						if (yi >= 1 && yi < 103 && (!Client.isLowDetail || (SceneRegion.Tiles_renderFlags[0][xi][yi] & 2) != 0 || (SceneRegion.Tiles_renderFlags[z][xi][yi] & 16) == 0)) {
							if (z < SceneRegion.Tiles_minPlane) {
								SceneRegion.Tiles_minPlane = z;
							}

							int underlayId = SceneRegion.underlayIds[z][xi][yi] & 255;
							int overlayId = class348.overlayIds[z][xi][yi] & 255;
							if (underlayId > 0 || overlayId > 0) {
								int swHeight = SceneRegion.Tiles_heights[z][xi][yi];
								int seHeight = SceneRegion.Tiles_heights[z][xi + 1][yi];
								int neHeight = SceneRegion.Tiles_heights[z][xi + 1][yi + 1];
								int nwHeight = SceneRegion.Tiles_heights[z][xi][yi + 1];
								int swColor = SceneRegion.tileColors[xi][yi];
								int seColor = SceneRegion.tileColors[xi + 1][yi];
								int neColor = SceneRegion.tileColors[xi + 1][yi + 1];
								int nwColor = SceneRegion.tileColors[xi][yi + 1];
								int rgb = -1;
								int underlayHsl = -1;

								if (underlayId > 0) {
									int avgHue = runningHues * 256 / runningMultiplier;
									int avgSat = runningSat / runningNumber;
									int avgLight = runningLight / runningNumber;
									rgb = DevicePcmPlayerProvider.hslToRgb(avgHue, avgSat, avgLight);
									avgHue = avgHue + SceneRegion.field550 & 255;
									avgLight += SceneRegion.field548;
									if (avgLight < 0) {
										avgLight = 0;
									} else if (avgLight > 255) {
										avgLight = 255;
									}

									underlayHsl = DevicePcmPlayerProvider.hslToRgb(avgHue, avgSat, avgLight);
								}

								if (z > 0) {
									boolean var47 = true;
									if (underlayId == 0 && SceneRegion.overlayPaths[z][xi][yi] != 0) {
										var47 = false;
									}

									if (overlayId > 0 && !UserComparator9.FloorUnderlayDefinition_get(overlayId - 1).hideUnderlay) {
										var47 = false;
									}

									if (var47 && swHeight == seHeight && neHeight == swHeight && swHeight == nwHeight) {
										var10000 = class51.field404[z][xi];
										var10000[yi] |= 2340;
									}
								}

								int underlayRgb = 0;
								if (underlayHsl != -1) {
									underlayRgb = Rasterizer3D.Rasterizer3D_colorPalette[Strings.method4220(underlayHsl, 96)];
								}

								if (overlayId == 0) {
									scene.addTile(z, xi, yi, 0, 0, -1, swHeight, seHeight, neHeight, nwHeight, Strings.method4220(rgb, swColor), Strings.method4220(rgb, seColor), Strings.method4220(rgb, neColor), Strings.method4220(rgb, nwColor), 0, 0, 0, 0, underlayRgb, 0);
								} else {
									int overlayPath = SceneRegion.overlayPaths[z][xi][yi] + 1;
									byte overlayRotation = SceneRegion.overlayRotations[z][xi][yi];
									FloorOverlayDefinition overlayDefinition = UserComparator9.FloorUnderlayDefinition_get(overlayId - 1);
									int overlayTexture = overlayDefinition.texture;
									int overlayHsl;

									if (overlayTexture >= 0) {
										rgb = Rasterizer3D.Rasterizer3D_textureLoader.getAverageTextureRGB(overlayTexture);
										overlayHsl = -1;
									} else if (overlayDefinition.primaryRgb == 16711935) {
										overlayHsl = -2;
										overlayTexture = -1;
										rgb = -2;
									} else {
										overlayHsl = DevicePcmPlayerProvider.hslToRgb(overlayDefinition.hue, overlayDefinition.saturation, overlayDefinition.lightness);
										int var39 = overlayDefinition.hue + SceneRegion.field550 & 255;
										int var40 = overlayDefinition.lightness + SceneRegion.field548;
										if (var40 < 0) {
											var40 = 0;
										} else if (var40 > 255) {
											var40 = 255;
										}

										rgb = DevicePcmPlayerProvider.hslToRgb(var39, overlayDefinition.saturation, var40);
									}

									int overlayRgb = 0;
									if (rgb != -2) {
										overlayRgb = Rasterizer3D.Rasterizer3D_colorPalette[class297.adjustHslListness(rgb, 96)];
									}

									if (overlayDefinition.secondaryRgb != -1) {
										int hue = overlayDefinition.secondaryHue + SceneRegion.field550 & 255;
										int lightness = overlayDefinition.secondaryLightness + SceneRegion.field548;
										if (lightness < 0) {
											lightness = 0;
										} else if (lightness > 255) {
											lightness = 255;
										}

										rgb = DevicePcmPlayerProvider.hslToRgb(hue, overlayDefinition.secondarySaturation, lightness);
										overlayRgb = Rasterizer3D.Rasterizer3D_colorPalette[class297.adjustHslListness(rgb, 96)];
									}

									scene.addTile(z, xi, yi, overlayPath, overlayRotation, overlayTexture, swHeight, seHeight, neHeight, nwHeight, Strings.method4220(rgb, swColor), Strings.method4220(rgb, seColor), Strings.method4220(rgb, neColor), Strings.method4220(rgb, nwColor), class297.adjustHslListness(overlayHsl, swColor), class297.adjustHslListness(overlayHsl, seColor), class297.adjustHslListness(overlayHsl, neColor), class297.adjustHslListness(overlayHsl, nwColor), underlayRgb, overlayRgb);
								}
							}
						}
					}
				}
			}

			for (var11 = 1; var11 < 103; ++var11) {
				for (var12 = 1; var12 < 103; ++var12) {
					if ((SceneRegion.Tiles_renderFlags[z][var12][var11] & 8) != 0) {
						var17 = 0;
					} else if (z > 0 && (SceneRegion.Tiles_renderFlags[1][var12][var11] & 2) != 0) {
						var17 = z - 1;
					} else {
						var17 = z;
					}

					scene.setTileMinPlane(z, var12, var11, var17);
				}
			}

			SceneRegion.underlayIds[z] = null;
			class348.overlayIds[z] = null;
			SceneRegion.overlayPaths[z] = null;
			SceneRegion.overlayRotations[z] = null;
			SoundCache.field1462[z] = null;
		}

		scene.method3262(-50, -10, -50);

		for (z = 0; z < 104; ++z) {
			for (var3 = 0; var3 < 104; ++var3) {
				if ((SceneRegion.Tiles_renderFlags[1][z][var3] & 2) == 2) {
					scene.setLinkBelow(z, var3);
				}
			}
		}

		z = 1;
		var3 = 2;
		var4 = 4;

		for (int renderLevel = 0; renderLevel < 4; ++renderLevel) {
			if (renderLevel > 0) {
				z <<= 3;
				var3 <<= 3;
				var4 <<= 3;
			}

			for (int zi = 0; zi <= renderLevel; ++zi) {
				for (int yi = 0; yi <= 104; ++yi) {
					for (int xi = 0; xi <= 104; ++xi) {
						short var46;
						if ((class51.field404[zi][xi][yi] & z) != 0) {
							var9 = yi;
							var10 = yi;
							var11 = zi;

							for (var12 = zi; var9 > 0 && (class51.field404[zi][xi][var9 - 1] & z) != 0; --var9) {
							}

							while (var10 < 104 && (class51.field404[zi][xi][var10 + 1] & z) != 0) {
								++var10;
							}

							label465:
							while (var11 > 0) {
								for (var13 = var9; var13 <= var10; ++var13) {
									if ((class51.field404[var11 - 1][xi][var13] & z) == 0) {
										break label465;
									}
								}

								--var11;
							}

							label454:
							while (var12 < renderLevel) {
								for (var13 = var9; var13 <= var10; ++var13) {
									if ((class51.field404[var12 + 1][xi][var13] & z) == 0) {
										break label454;
									}
								}

								++var12;
							}

							var13 = (var10 - var9 + 1) * (var12 + 1 - var11);
							if (var13 >= 8) {
								var46 = 240;
								var15 = SceneRegion.Tiles_heights[var12][xi][var9] - var46;
								var16 = SceneRegion.Tiles_heights[var11][xi][var9];
								Scene.Scene_addOccluder(renderLevel, 1, xi * 128, xi * 128, var9 * 128, var10 * 128 + 128, var15, var16);

								for (var17 = var11; var17 <= var12; ++var17) {
									for (var18 = var9; var18 <= var10; ++var18) {
										var10000 = class51.field404[var17][xi];
										var10000[var18] &= ~z;
									}
								}
							}
						}

						if ((class51.field404[zi][xi][yi] & var3) != 0) {
							var9 = xi;
							var10 = xi;
							var11 = zi;

							for (var12 = zi; var9 > 0 && (class51.field404[zi][var9 - 1][yi] & var3) != 0; --var9) {
							}

							while (var10 < 104 && (class51.field404[zi][var10 + 1][yi] & var3) != 0) {
								++var10;
							}

							label518:
							while (var11 > 0) {
								for (var13 = var9; var13 <= var10; ++var13) {
									if ((class51.field404[var11 - 1][var13][yi] & var3) == 0) {
										break label518;
									}
								}

								--var11;
							}

							label507:
							while (var12 < renderLevel) {
								for (var13 = var9; var13 <= var10; ++var13) {
									if ((class51.field404[var12 + 1][var13][yi] & var3) == 0) {
										break label507;
									}
								}

								++var12;
							}

							var13 = (var12 + 1 - var11) * (var10 - var9 + 1);
							if (var13 >= 8) {
								var46 = 240;
								var15 = SceneRegion.Tiles_heights[var12][var9][yi] - var46;
								var16 = SceneRegion.Tiles_heights[var11][var9][yi];
								Scene.Scene_addOccluder(renderLevel, 2, var9 * 128, var10 * 128 + 128, yi * 128, yi * 128, var15, var16);

								for (var17 = var11; var17 <= var12; ++var17) {
									for (var18 = var9; var18 <= var10; ++var18) {
										var10000 = class51.field404[var17][var18];
										var10000[yi] &= ~var3;
									}
								}
							}
						}

						if ((class51.field404[zi][xi][yi] & var4) != 0) {
							var9 = xi;
							var10 = xi;
							var11 = yi;

							for (var12 = yi; var11 > 0 && (class51.field404[zi][xi][var11 - 1] & var4) != 0; --var11) {
							}

							while (var12 < 104 && (class51.field404[zi][xi][var12 + 1] & var4) != 0) {
								++var12;
							}

							label571:
							while (var9 > 0) {
								for (var13 = var11; var13 <= var12; ++var13) {
									if ((class51.field404[zi][var9 - 1][var13] & var4) == 0) {
										break label571;
									}
								}

								--var9;
							}

							label560:
							while (var10 < 104) {
								for (var13 = var11; var13 <= var12; ++var13) {
									if ((class51.field404[zi][var10 + 1][var13] & var4) == 0) {
										break label560;
									}
								}

								++var10;
							}

							if ((var12 - var11 + 1) * (var10 - var9 + 1) >= 4) {
								var13 = SceneRegion.Tiles_heights[zi][var9][var11];
								Scene.Scene_addOccluder(renderLevel, 4, var9 * 128, var10 * 128 + 128, var11 * 128, var12 * 128 + 128, var13, var13);

								for (var14 = var9; var14 <= var10; ++var14) {
									for (var15 = var11; var15 <= var12; ++var15) {
										var10000 = class51.field404[zi][var14];
										var10000[var15] &= ~var4;
									}
								}
							}
						}
					}
				}
			}
		}

	}

	@ObfuscatedName("gs")
	@ObfuscatedSignature(
		signature = "(I)V",
		garbageValue = "-2010586363"
	)
	static final void method249() {
		for (GraphicsObject var0 = (GraphicsObject)Client.graphicsObjects.last(); var0 != null; var0 = (GraphicsObject)Client.graphicsObjects.previous()) {
			if (var0.plane == ScriptEvent.Client_plane && !var0.isFinished) {
				if (Client.cycle >= var0.cycleStart) {
					var0.advance(Client.field741);
					if (var0.isFinished) {
						var0.remove();
					} else {
						GrandExchangeOfferWorldComparator.scene.drawEntity(var0.plane, var0.x, var0.y, var0.height, 60, var0, 0, -1L, false);
					}
				}
			} else {
				var0.remove();
			}
		}

	}

	@ObfuscatedName("jw")
	@ObfuscatedSignature(
		signature = "(Lhe;I)Z",
		garbageValue = "-74932209"
	)
	@Export("runCs1")
	static final boolean runCs1(Widget var0) {
		if (var0.cs1Comparisons == null) {
			return false;
		} else {
			for (int var1 = 0; var1 < var0.cs1Comparisons.length; ++var1) {
				int var2 = SceneRegion.method1227(var0, var1);
				int var3 = var0.cs1ComparisonValues[var1];
				if (var0.cs1Comparisons[var1] == 2) {
					if (var2 >= var3) {
						return false;
					}
				} else if (var0.cs1Comparisons[var1] == 3) {
					if (var2 <= var3) {
						return false;
					}
				} else if (var0.cs1Comparisons[var1] == 4) {
					if (var3 == var2) {
						return false;
					}
				} else if (var2 != var3) {
					return false;
				}
			}

			return true;
		}
	}
}

import java.io.DataInputStream;
import java.net.URL;
import net.runelite.mapping.Export;
import net.runelite.mapping.Implements;
import net.runelite.mapping.ObfuscatedGetter;
import net.runelite.mapping.ObfuscatedName;
import net.runelite.mapping.ObfuscatedSignature;

@ObfuscatedName("ha")
@Implements("WorldMapDecorationType")
public enum WorldMapDecorationType implements Enumerated {
	@ObfuscatedName("x")
	@ObfuscatedSignature(
		signature = "Lha;"
	)
	field2732(0, 0),
	@ObfuscatedName("m")
	@ObfuscatedSignature(
		signature = "Lha;"
	)
	field2718(1, 0),
	@ObfuscatedName("k")
	@ObfuscatedSignature(
		signature = "Lha;"
	)
	field2728(2, 0),
	@ObfuscatedName("d")
	@ObfuscatedSignature(
		signature = "Lha;"
	)
	field2742(3, 0),
	@ObfuscatedName("w")
	@ObfuscatedSignature(
		signature = "Lha;"
	)
	field2722(9, 2),
	@ObfuscatedName("v")
	@ObfuscatedSignature(
		signature = "Lha;"
	)
	field2723(4, 1),
	@ObfuscatedName("q")
	@ObfuscatedSignature(
		signature = "Lha;"
	)
	field2724(5, 1),
	@ObfuscatedName("z")
	@ObfuscatedSignature(
		signature = "Lha;"
	)
	field2721(6, 1),
	@ObfuscatedName("t")
	@ObfuscatedSignature(
		signature = "Lha;"
	)
	field2726(7, 1),
	@ObfuscatedName("e")
	@ObfuscatedSignature(
		signature = "Lha;"
	)
	field2730(8, 1),
	@ObfuscatedName("s")
	@ObfuscatedSignature(
		signature = "Lha;"
	)
	field2734(12, 2),
	@ObfuscatedName("p")
	@ObfuscatedSignature(
		signature = "Lha;"
	)
	field2729(13, 2),
	@ObfuscatedName("n")
	@ObfuscatedSignature(
		signature = "Lha;"
	)
	field2720(14, 2),
	@ObfuscatedName("u")
	@ObfuscatedSignature(
		signature = "Lha;"
	)
	field2731(15, 2),
	@ObfuscatedName("h")
	@ObfuscatedSignature(
		signature = "Lha;"
	)
	field2725(16, 2),
	@ObfuscatedName("g")
	@ObfuscatedSignature(
		signature = "Lha;"
	)
	field2738(17, 2),
	@ObfuscatedName("i")
	@ObfuscatedSignature(
		signature = "Lha;"
	)
	field2737(18, 2),
	@ObfuscatedName("a")
	@ObfuscatedSignature(
		signature = "Lha;"
	)
	field2735(19, 2),
	@ObfuscatedName("b")
	@ObfuscatedSignature(
		signature = "Lha;"
	)
	field2736(20, 2),
	@ObfuscatedName("l")
	@ObfuscatedSignature(
		signature = "Lha;"
	)
	field2733(21, 2),
	@ObfuscatedName("r")
	@ObfuscatedSignature(
		signature = "Lha;"
	)
	field2719(10, 2),
	@ObfuscatedName("o")
	@ObfuscatedSignature(
		signature = "Lha;"
	)
	field2739(11, 2),
	@ObfuscatedName("c")
	@ObfuscatedSignature(
		signature = "Lha;"
	)
	field2740(22, 3);

	@ObfuscatedName("j")
	@ObfuscatedGetter(
		intValue = -1867992849
	)
	@Export("id")
	public final int id;

	@ObfuscatedSignature(
		signature = "(II)V",
		garbageValue = "0"
	)
	WorldMapDecorationType(int var3, int var4) {
		this.id = var3;
	}

	@ObfuscatedName("d")
	@ObfuscatedSignature(
		signature = "(B)I",
		garbageValue = "56"
	)
	@Export("rsOrdinal")
	public int rsOrdinal() {
		return this.id;
	}

	@ObfuscatedName("x")
	@ObfuscatedSignature(
		signature = "(Ljava/lang/String;Ljava/lang/Throwable;I)V",
		garbageValue = "-1131770525"
	)
	@Export("RunException_sendStackTrace")
	public static void RunException_sendStackTrace(String var0, Throwable var1) {
		if (var1 != null) {
			var1.printStackTrace();
		} else {
			try {
				String var2 = "";
				if (var1 != null) {
					var2 = NetCache.method4393(var1);
				}

				if (var0 != null) {
					if (var1 != null) {
						var2 = var2 + " | ";
					}

					var2 = var2 + var0;
				}

				System.out.println("Error: " + var2);
				var2 = var2.replace(':', '.');
				var2 = var2.replace('@', '_');
				var2 = var2.replace('&', '_');
				var2 = var2.replace('#', '_');
				if (RunException.RunException_applet == null) {
					return;
				}

				URL var3 = new URL(RunException.RunException_applet.getCodeBase(), "clienterror.ws?c=" + RunException.RunException_revision + "&u=" + RunException.localPlayerName + "&v1=" + TaskHandler.javaVendor + "&v2=" + TaskHandler.javaVersion + "&ct=" + RunException.clientType + "&e=" + var2);
				DataInputStream var4 = new DataInputStream(var3.openStream());
				var4.read();
				var4.close();
			} catch (Exception var5) {
			}

		}
	}

	@ObfuscatedName("z")
	static boolean method4212(long var0) {
		return (int)(var0 >>> 16 & 1L) == 1;
	}

	@ObfuscatedName("jm")
	@ObfuscatedSignature(
		signature = "([Lhe;IIIIIIII)V",
		garbageValue = "635809234"
	)
	@Export("updateInterface")
	static final void updateInterface(Widget[] var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7) {
		for (int var8 = 0; var8 < var0.length; ++var8) {
			Widget mouse = var0[var8];
			if (mouse != null && mouse.parentId == var1 && (!mouse.isIf3 || mouse.type == 0 || mouse.hasListener || ScriptEvent.getWidgetClickMask(mouse) != 0 || mouse == Client.clickedWidgetParent || mouse.contentType == 1338)) {
				if (mouse.isIf3) {
					if (AbstractWorldMapData.isComponentHidden(mouse)) {
						continue;
					}
				} else if (mouse.type == 0 && mouse != class9.mousedOverWidgetIf1 && AbstractWorldMapData.isComponentHidden(mouse)) {
					continue;
				}

				int var10 = mouse.x + var6;
				int var11 = var7 + mouse.y;
				int var12;
				int var13;
				int var14;
				int var15;
				int var17;
				int var18;
				if (mouse.type == 2) {
					var12 = var2;
					var13 = var3;
					var14 = var4;
					var15 = var5;
				} else {
					int var16;
					if (mouse.type == 9) {
						var16 = var10;
						var17 = var11;
						var18 = var10 + mouse.width;
						int var19 = var11 + mouse.height;
						if (var18 < var10) {
							var16 = var18;
							var18 = var10;
						}

						if (var19 < var11) {
							var17 = var19;
							var19 = var11;
						}

						++var18;
						++var19;
						var12 = var16 > var2 ? var16 : var2;
						var13 = var17 > var3 ? var17 : var3;
						var14 = var18 < var4 ? var18 : var4;
						var15 = var19 < var5 ? var19 : var5;
					} else {
						var16 = var10 + mouse.width;
						var17 = var11 + mouse.height;
						var12 = var10 > var2 ? var10 : var2;
						var13 = var11 > var3 ? var11 : var3;
						var14 = var16 < var4 ? var16 : var4;
						var15 = var17 < var5 ? var17 : var5;
					}
				}

				if (mouse == Client.clickedWidget) {
					Client.field861 = true;
					Client.field834 = var10;
					Client.field863 = var11;
				}

				boolean var32 = false;
				if (mouse.field2641) {
					switch(Client.field842) {
					case 0:
						var32 = true;
					case 1:
					default:
						break;
					case 2:
						if (Client.field843 == mouse.id >>> 16) {
							var32 = true;
						}
						break;
					case 3:
						if (mouse.id == Client.field843) {
							var32 = true;
						}
					}
				}

				if (var32 || !mouse.isIf3 || var12 < var14 && var13 < var15) {
					if (mouse.isIf3) {
						ScriptEvent var26;
						if (mouse.noClickThrough) {
							if (MouseHandler.MouseHandler_x >= var12 && MouseHandler.MouseHandler_y >= var13 && MouseHandler.MouseHandler_x < var14 && MouseHandler.MouseHandler_y < var15) {
								for (var26 = (ScriptEvent)Client.scriptEvents.last(); var26 != null; var26 = (ScriptEvent)Client.scriptEvents.previous()) {
									if (var26.isMouseInputEvent) {
										var26.remove();
										var26.widget.containsMouse = false;
									}
								}

								if (MilliClock.widgetDragDuration == 0) {
									Client.clickedWidget = null;
									Client.clickedWidgetParent = null;
								}

								if (!Client.isMenuOpen) {
									Tile.addCancelMenuEntry();
								}
							}
						} else if (mouse.noScrollThrough && MouseHandler.MouseHandler_x >= var12 && MouseHandler.MouseHandler_y >= var13 && MouseHandler.MouseHandler_x < var14 && MouseHandler.MouseHandler_y < var15) {
							for (var26 = (ScriptEvent)Client.scriptEvents.last(); var26 != null; var26 = (ScriptEvent)Client.scriptEvents.previous()) {
								if (var26.isMouseInputEvent && var26.widget.onScroll == var26.args) {
									var26.remove();
								}
							}
						}
					}

					var17 = MouseHandler.MouseHandler_x;
					var18 = MouseHandler.MouseHandler_y;
					if (MouseHandler.MouseHandler_lastButton != 0) {
						var17 = MouseHandler.MouseHandler_lastPressedX;
						var18 = MouseHandler.MouseHandler_lastPressedY;
					}

					boolean var33 = var17 >= var12 && var18 >= var13 && var17 < var14 && var18 < var15;
					if (mouse.contentType == 1337) {
						if (!Client.isLoading && !Client.isMenuOpen && var33) {
							WorldMapRectangle.addSceneMenuOptions(var17, var18, var12, var13);
						}
					} else if (mouse.contentType == 1338) {
						class3.checkIfMinimapClicked(mouse, var10, var11);
					} else {
						if (mouse.contentType == 1400) {
							GrandExchangeOfferAgeComparator.worldMap.onCycle(MouseHandler.MouseHandler_x, MouseHandler.MouseHandler_y, var33, var10, var11, mouse.width, mouse.height);
						}

						if (!Client.isMenuOpen && var33) {
							if (mouse.contentType == 1400) {
								GrandExchangeOfferAgeComparator.worldMap.addElementMenuOptions(var10, var11, mouse.width, mouse.height, var17, var18);
							} else {
								WorldMapLabelSize.method288(mouse, var17 - var10, var18 - var11);
							}
						}

						boolean var21;
						int var23;
						if (var32) {
							for (int var20 = 0; var20 < mouse.field2555.length; ++var20) {
								var21 = false;
								boolean var22 = false;
								if (!var21 && mouse.field2555[var20] != null) {
									for (var23 = 0; var23 < mouse.field2555[var20].length; ++var23) {
										boolean var24 = false;
										if (mouse.field2706 != null) {
											var24 = KeyHandler.KeyHandler_pressedKeys[mouse.field2555[var20][var23]];
										}

										if (Messages.method2322(mouse.field2555[var20][var23]) || var24) {
											var21 = true;
											if (mouse.field2706 != null && mouse.field2706[var20] > Client.cycle) {
												break;
											}

											byte var25 = mouse.field2643[var20][var23];
											if (var25 == 0 || ((var25 & 8) == 0 || !KeyHandler.KeyHandler_pressedKeys[86] && !KeyHandler.KeyHandler_pressedKeys[82] && !KeyHandler.KeyHandler_pressedKeys[81]) && ((var25 & 2) == 0 || KeyHandler.KeyHandler_pressedKeys[86]) && ((var25 & 1) == 0 || KeyHandler.KeyHandler_pressedKeys[82]) && ((var25 & 4) == 0 || KeyHandler.KeyHandler_pressedKeys[81])) {
												var22 = true;
												break;
											}
										}
									}
								}

								if (var22) {
									if (var20 < 10) {
										ClientPacket.widgetDefaultMenuAction(var20 + 1, mouse.id, mouse.childIndex, mouse.itemId, "");
									} else if (var20 == 10) {
										GrandExchangeOffer.Widget_runOnTargetLeave();
										WorldMapData_1.selectSpell(mouse.id, mouse.childIndex, WorldMapRectangle.method388(ScriptEvent.getWidgetClickMask(mouse)), mouse.itemId);
										Client.selectedSpellActionName = PrivateChatMode.method5965(mouse);
										if (Client.selectedSpellActionName == null) {
											Client.selectedSpellActionName = "null";
										}

										Client.selectedSpellName = mouse.dataText + class297.colorStartTag(16777215);
									}

									var23 = mouse.field2644[var20];
									if (mouse.field2706 == null) {
										mouse.field2706 = new int[mouse.field2555.length];
									}

									if (mouse.field2695 == null) {
										mouse.field2695 = new int[mouse.field2555.length];
									}

									if (var23 != 0) {
										if (mouse.field2706[var20] == 0) {
											mouse.field2706[var20] = var23 + Client.cycle + mouse.field2695[var20];
										} else {
											mouse.field2706[var20] = var23 + Client.cycle;
										}
									} else {
										mouse.field2706[var20] = Integer.MAX_VALUE;
									}
								}

								if (!var21 && mouse.field2706 != null) {
									mouse.field2706[var20] = 0;
								}
							}
						}

						if (mouse.isIf3) {
							if (MouseHandler.MouseHandler_x >= var12 && MouseHandler.MouseHandler_y >= var13 && MouseHandler.MouseHandler_x < var14 && MouseHandler.MouseHandler_y < var15) {
								var33 = true;
							} else {
								var33 = false;
							}

							boolean var34 = false;
							if ((MouseHandler.MouseHandler_currentButton == 1 || !class217.mouseCam && MouseHandler.MouseHandler_currentButton == 4) && var33) {
								var34 = true;
							}

							var21 = false;
							if ((MouseHandler.MouseHandler_lastButton == 1 || !class217.mouseCam && MouseHandler.MouseHandler_lastButton == 4) && MouseHandler.MouseHandler_lastPressedX >= var12 && MouseHandler.MouseHandler_lastPressedY >= var13 && MouseHandler.MouseHandler_lastPressedX < var14 && MouseHandler.MouseHandler_lastPressedY < var15) {
								var21 = true;
							}

							if (var21) {
								Player.clickWidget(mouse, MouseHandler.MouseHandler_lastPressedX - var10, MouseHandler.MouseHandler_lastPressedY - var11);
							}

							if (mouse.contentType == 1400) {
								GrandExchangeOfferAgeComparator.worldMap.method6420(var17, var18, var33 & var34, var33 & var21);
							}

							if (Client.clickedWidget != null && mouse != Client.clickedWidget && var33 && GrandExchangeOfferUnitPriceComparator.method218(ScriptEvent.getWidgetClickMask(mouse))) {
								Client.draggedOnWidget = mouse;
							}

							if (mouse == Client.clickedWidgetParent) {
								Client.field732 = true;
								Client.field859 = var10;
								Client.field881 = var11;
							}

							if (mouse.hasListener) {
								ScriptEvent var27;
								if (var33 && Client.mouseWheelRotation != 0 && mouse.onScroll != null) {
									var27 = new ScriptEvent();
									var27.isMouseInputEvent = true;
									var27.widget = mouse;
									var27.mouseY = Client.mouseWheelRotation;
									var27.args = mouse.onScroll;
									Client.scriptEvents.addFirst(var27);
								}

								if (Client.clickedWidget != null || class236.dragInventoryWidget != null || Client.isMenuOpen) {
									var21 = false;
									var34 = false;
									var33 = false;
								}

								if (!mouse.isClicked && var21) {
									mouse.isClicked = true;
									if (mouse.onClick != null) {
										var27 = new ScriptEvent();
										var27.isMouseInputEvent = true;
										var27.widget = mouse;
										var27.mouseX = MouseHandler.MouseHandler_lastPressedX - var10;
										var27.mouseY = MouseHandler.MouseHandler_lastPressedY - var11;
										var27.args = mouse.onClick;
										Client.scriptEvents.addFirst(var27);
									}
								}

								if (mouse.isClicked && var34 && mouse.onClickRepeat != null) {
									var27 = new ScriptEvent();
									var27.isMouseInputEvent = true;
									var27.widget = mouse;
									var27.mouseX = MouseHandler.MouseHandler_x - var10;
									var27.mouseY = MouseHandler.MouseHandler_y - var11;
									var27.args = mouse.onClickRepeat;
									Client.scriptEvents.addFirst(var27);
								}

								if (mouse.isClicked && !var34) {
									mouse.isClicked = false;
									if (mouse.onRelease != null) {
										var27 = new ScriptEvent();
										var27.isMouseInputEvent = true;
										var27.widget = mouse;
										var27.mouseX = MouseHandler.MouseHandler_x - var10;
										var27.mouseY = MouseHandler.MouseHandler_y - var11;
										var27.args = mouse.onRelease;
										Client.field919.addFirst(var27);
									}
								}

								if (var34 && mouse.onHold != null) {
									var27 = new ScriptEvent();
									var27.isMouseInputEvent = true;
									var27.widget = mouse;
									var27.mouseX = MouseHandler.MouseHandler_x - var10;
									var27.mouseY = MouseHandler.MouseHandler_y - var11;
									var27.args = mouse.onHold;
									Client.scriptEvents.addFirst(var27);
								}

								if (!mouse.containsMouse && var33) {
									mouse.containsMouse = true;
									if (mouse.onMouseOver != null) {
										var27 = new ScriptEvent();
										var27.isMouseInputEvent = true;
										var27.widget = mouse;
										var27.mouseX = MouseHandler.MouseHandler_x - var10;
										var27.mouseY = MouseHandler.MouseHandler_y - var11;
										var27.args = mouse.onMouseOver;
										Client.scriptEvents.addFirst(var27);
									}
								}

								if (mouse.containsMouse && var33 && mouse.onMouseRepeat != null) {
									var27 = new ScriptEvent();
									var27.isMouseInputEvent = true;
									var27.widget = mouse;
									var27.mouseX = MouseHandler.MouseHandler_x - var10;
									var27.mouseY = MouseHandler.MouseHandler_y - var11;
									var27.args = mouse.onMouseRepeat;
									Client.scriptEvents.addFirst(var27);
								}

								if (mouse.containsMouse && !var33) {
									mouse.containsMouse = false;
									if (mouse.onMouseLeave != null) {
										var27 = new ScriptEvent();
										var27.isMouseInputEvent = true;
										var27.widget = mouse;
										var27.mouseX = MouseHandler.MouseHandler_x - var10;
										var27.mouseY = MouseHandler.MouseHandler_y - var11;
										var27.args = mouse.onMouseLeave;
										Client.field919.addFirst(var27);
									}
								}

								if (mouse.onTimer != null) {
									var27 = new ScriptEvent();
									var27.widget = mouse;
									var27.args = mouse.onTimer;
									Client.field727.addFirst(var27);
								}

								ScriptEvent var30;
								int var35;
								int var36;
								if (mouse.onVarTransmit != null && Client.field772 > mouse.field2701) {
									if (mouse.varTransmitTriggers != null && Client.field772 - mouse.field2701 <= 32) {
										label887:
										for (var35 = mouse.field2701; var35 < Client.field772; ++var35) {
											var23 = Client.field866[var35 & 31];

											for (var36 = 0; var36 < mouse.varTransmitTriggers.length; ++var36) {
												if (var23 == mouse.varTransmitTriggers[var36]) {
													var30 = new ScriptEvent();
													var30.widget = mouse;
													var30.args = mouse.onVarTransmit;
													Client.scriptEvents.addFirst(var30);
													break label887;
												}
											}
										}
									} else {
										var27 = new ScriptEvent();
										var27.widget = mouse;
										var27.args = mouse.onVarTransmit;
										Client.scriptEvents.addFirst(var27);
									}

									mouse.field2701 = Client.field772;
								}

								if (mouse.onInvTransmit != null && Client.field869 > mouse.field2620) {
									if (mouse.invTransmitTriggers != null && Client.field869 - mouse.field2620 <= 32) {
										label863:
										for (var35 = mouse.field2620; var35 < Client.field869; ++var35) {
											var23 = Client.changedItemContainers[var35 & 31];

											for (var36 = 0; var36 < mouse.invTransmitTriggers.length; ++var36) {
												if (var23 == mouse.invTransmitTriggers[var36]) {
													var30 = new ScriptEvent();
													var30.widget = mouse;
													var30.args = mouse.onInvTransmit;
													Client.scriptEvents.addFirst(var30);
													break label863;
												}
											}
										}
									} else {
										var27 = new ScriptEvent();
										var27.widget = mouse;
										var27.args = mouse.onInvTransmit;
										Client.scriptEvents.addFirst(var27);
									}

									mouse.field2620 = Client.field869;
								}

								if (mouse.onStatTransmit != null && Client.changedSkillsCount > mouse.field2588) {
									if (mouse.statTransmitTriggers != null && Client.changedSkillsCount - mouse.field2588 <= 32) {
										label839:
										for (var35 = mouse.field2588; var35 < Client.changedSkillsCount; ++var35) {
											var23 = Client.changedSkills[var35 & 31];

											for (var36 = 0; var36 < mouse.statTransmitTriggers.length; ++var36) {
												if (var23 == mouse.statTransmitTriggers[var36]) {
													var30 = new ScriptEvent();
													var30.widget = mouse;
													var30.args = mouse.onStatTransmit;
													Client.scriptEvents.addFirst(var30);
													break label839;
												}
											}
										}
									} else {
										var27 = new ScriptEvent();
										var27.widget = mouse;
										var27.args = mouse.onStatTransmit;
										Client.scriptEvents.addFirst(var27);
									}

									mouse.field2588 = Client.changedSkillsCount;
								}

								if (Client.chatCycle > mouse.field2700 && mouse.onChatTransmit != null) {
									var27 = new ScriptEvent();
									var27.widget = mouse;
									var27.args = mouse.onChatTransmit;
									Client.scriptEvents.addFirst(var27);
								}

								if (Client.field873 > mouse.field2700 && mouse.onFriendTransmit != null) {
									var27 = new ScriptEvent();
									var27.widget = mouse;
									var27.args = mouse.onFriendTransmit;
									Client.scriptEvents.addFirst(var27);
								}

								if (Client.field874 > mouse.field2700 && mouse.onClanTransmit != null) {
									var27 = new ScriptEvent();
									var27.widget = mouse;
									var27.args = mouse.onClanTransmit;
									Client.scriptEvents.addFirst(var27);
								}

								if (Client.field774 > mouse.field2700 && mouse.onStockTransmit != null) {
									var27 = new ScriptEvent();
									var27.widget = mouse;
									var27.args = mouse.onStockTransmit;
									Client.scriptEvents.addFirst(var27);
								}

								if (Client.field726 > mouse.field2700 && mouse.field2691 != null) {
									var27 = new ScriptEvent();
									var27.widget = mouse;
									var27.args = mouse.field2691;
									Client.scriptEvents.addFirst(var27);
								}

								if (Client.field877 > mouse.field2700 && mouse.onMiscTransmit != null) {
									var27 = new ScriptEvent();
									var27.widget = mouse;
									var27.args = mouse.onMiscTransmit;
									Client.scriptEvents.addFirst(var27);
								}

								mouse.field2700 = Client.cycleCntr;
								if (mouse.onKey != null) {
									for (var35 = 0; var35 < Client.field901; ++var35) {
										ScriptEvent var31 = new ScriptEvent();
										var31.widget = mouse;
										var31.keyTyped = Client.field872[var35];
										var31.keyPressed = Client.field902[var35];
										var31.args = mouse.onKey;
										Client.scriptEvents.addFirst(var31);
									}
								}
							}
						}

						if (!mouse.isIf3) {
							if (Client.clickedWidget != null || class236.dragInventoryWidget != null || Client.isMenuOpen) {
								continue;
							}

							if ((mouse.mouseOverRedirect >= 0 || mouse.mouseOverColor != 0) && MouseHandler.MouseHandler_x >= var12 && MouseHandler.MouseHandler_y >= var13 && MouseHandler.MouseHandler_x < var14 && MouseHandler.MouseHandler_y < var15) {
								if (mouse.mouseOverRedirect >= 0) {
									class9.mousedOverWidgetIf1 = var0[mouse.mouseOverRedirect];
								} else {
									class9.mousedOverWidgetIf1 = mouse;
								}
							}

							if (mouse.type == 8 && MouseHandler.MouseHandler_x >= var12 && MouseHandler.MouseHandler_y >= var13 && MouseHandler.MouseHandler_x < var14 && MouseHandler.MouseHandler_y < var15) {
								Language.field2368 = mouse;
							}

							if (mouse.scrollHeight > mouse.height) {
								ViewportMouse.method3092(mouse, var10 + mouse.width, var11, mouse.height, mouse.scrollHeight, MouseHandler.MouseHandler_x, MouseHandler.MouseHandler_y);
							}
						}

						if (mouse.type == 0) {
							updateInterface(var0, mouse.id, var12, var13, var14, var15, var10 - mouse.scrollX, var11 - mouse.scrollY);
							if (mouse.children != null) {
								updateInterface(mouse.children, mouse.id, var12, var13, var14, var15, var10 - mouse.scrollX, var11 - mouse.scrollY);
							}

							InterfaceParent var28 = (InterfaceParent)Client.interfaceParents.get((long) mouse.id);
							if (var28 != null) {
								if (var28.type == 0 && MouseHandler.MouseHandler_x >= var12 && MouseHandler.MouseHandler_y >= var13 && MouseHandler.MouseHandler_x < var14 && MouseHandler.MouseHandler_y < var15 && !Client.isMenuOpen) {
									for (ScriptEvent var29 = (ScriptEvent)Client.scriptEvents.last(); var29 != null; var29 = (ScriptEvent)Client.scriptEvents.previous()) {
										if (var29.isMouseInputEvent) {
											var29.remove();
											var29.widget.containsMouse = false;
										}
									}

									if (MilliClock.widgetDragDuration == 0) {
										Client.clickedWidget = null;
										Client.clickedWidgetParent = null;
									}

									if (!Client.isMenuOpen) {
										Tile.addCancelMenuEntry();
									}
								}

								class2.updateRootInterface(var28.group, var12, var13, var14, var15, var10, var11);
							}
						}
					}
				}
			}
		}

	}
}

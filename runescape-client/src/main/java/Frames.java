import net.runelite.mapping.Export;
import net.runelite.mapping.Implements;
import net.runelite.mapping.ObfuscatedName;
import net.runelite.mapping.ObfuscatedSignature;

@ObfuscatedName("ef")
@Implements("Frames")
public class Frames extends DualNode {
	@ObfuscatedName("x")
	@ObfuscatedSignature(
		signature = "[Leh;"
	)
	@Export("frames")
	Animation[] frames;

	@ObfuscatedSignature(
		signature = "(Liy;Liy;IZ)V",
		garbageValue = "0"
	)
	public Frames(AbstractArchive animationsArchive, AbstractArchive skeletonsArchive, int id) {
		NodeDeque var5 = new NodeDeque();
		int fileCount = animationsArchive.getGroupFileCount(id);
		this.frames = new Animation[fileCount];
		int[] fileIds = animationsArchive.getGroupFileIds(id);

		for (int i = 0; i < fileIds.length; ++i) {
			byte[] contents = animationsArchive.takeFile(id, fileIds[i]);
			Skeleton skeleton = null;
			int var11 = (contents[0] & 255) << 8 | contents[1] & 255;

			for (Skeleton var12 = (Skeleton)var5.last(); var12 != null; var12 = (Skeleton)var5.previous()) {
				if (var11 == var12.id) {
					skeleton = var12;
					break;
				}
			}

			if (skeleton == null) {
				byte[] var13 = skeletonsArchive.getFile(var11, 0);
				skeleton = new Skeleton(var11, var13);
				var5.addFirst(skeleton);
			}

			this.frames[fileIds[i]] = new Animation(contents, skeleton);
		}

	}

	@ObfuscatedName("x")
	@ObfuscatedSignature(
		signature = "(II)Z",
		garbageValue = "861900150"
	)
	@Export("hasAlphaTransform")
	public boolean hasAlphaTransform(int var1) {
		return this.frames[var1].hasAlphaTransform;
	}

	@ObfuscatedName("k")
	@ObfuscatedSignature(
		signature = "(Lih;IIIBZI)V",
		garbageValue = "-626508532"
	)
	@Export("requestNetFile")
	static void requestNetFile(Archive var0, int var1, int var2, int var3, byte var4, boolean var5) {
		long var6 = (long)((var1 << 16) + var2);
		NetFileRequest var8 = (NetFileRequest)NetCache.NetCache_pendingPriorityWrites.get(var6);
		if (var8 == null) {
			var8 = (NetFileRequest)NetCache.NetCache_pendingPriorityResponses.get(var6);
			if (var8 == null) {
				var8 = (NetFileRequest)NetCache.NetCache_pendingWrites.get(var6);
				if (var8 != null) {
					if (var5) {
						var8.removeDual();
						NetCache.NetCache_pendingPriorityWrites.put(var8, var6);
						--NetCache.NetCache_pendingWritesCount;
						++NetCache.NetCache_pendingPriorityWritesCount;
					}

				} else {
					if (!var5) {
						var8 = (NetFileRequest)NetCache.NetCache_pendingResponses.get(var6);
						if (var8 != null) {
							return;
						}
					}

					var8 = new NetFileRequest();
					var8.archive = var0;
					var8.crc = var3;
					var8.padding = var4;
					if (var5) {
						NetCache.NetCache_pendingPriorityWrites.put(var8, var6);
						++NetCache.NetCache_pendingPriorityWritesCount;
					} else {
						NetCache.NetCache_pendingWritesQueue.addFirst(var8);
						NetCache.NetCache_pendingWrites.put(var8, var6);
						++NetCache.NetCache_pendingWritesCount;
					}

				}
			}
		}
	}
}

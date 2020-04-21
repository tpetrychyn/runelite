/*
 * Copyright (c) 2016-2017, Adam <Adam@sigterm.info>
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
package net.runelite.cache.definitions;

import lombok.Data;
import net.runelite.cache.IndexType;
import net.runelite.cache.definitions.loaders.FrameLoader;
import net.runelite.cache.definitions.loaders.FramemapLoader;
import net.runelite.cache.fs.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class SequenceDefinition {
    private final int id;
    public int[] frameIds; // top 16 bits are FrameDefinition ids
    public int[] chatFrameIds;
    public int[] frameLengths;
    public int[] frameSounds;
    public int frameCount = -1;
    public int[] interleaveLeave;
    public boolean stretches = false;
    public int forcedPriority = 5;
    public int leftHandItem = -1;
    public int rightHandItem = -1;
    public int maxLoops = 99;
    public int precedenceAnimating = -1;
    public int priority = -1;
    public int replayMode = 2;

    private static Map<Integer, FramesDefinition> framesCache = new HashMap<>();

    private FramesDefinition getFrames(int id) {
    	FramesDefinition frames = framesCache.get(id);
    	if (frames != null) {
    		return frames;
		}
        try {
            Store store = StoreProvider.getStore();

            Storage storage = store.getStorage();
            Index animationsArchive = store.getIndex(IndexType.FRAMES);
            Index skeletonsArchive = store.getIndex(IndexType.FRAMEMAPS);

            ArchiveFiles archiveFiles;

            Archive archive = animationsArchive.getArchive(id);
            byte[] archiveData = storage.loadArchive(archive);
            archiveFiles = archive.getFiles(archiveData);

            List<AnimationDefinition> animations = new ArrayList<>();
            for (FSFile archiveFile : archiveFiles.getFiles()) {
				byte[] contents = archiveFile.getContents();

				int skeletonId = (contents[0] & 0xff) << 8 | contents[1] & 0xff;

				Archive skeletonGroup = skeletonsArchive.getArchive(skeletonId);
				archiveData = storage.loadArchive(skeletonGroup);
				byte[] skeletonContents = skeletonGroup.decompress(archiveData);

				FramemapLoader fmloader = new FramemapLoader();
				SkeletonDefinition framemap = fmloader.load(skeletonGroup.getArchiveId(), skeletonContents);

				FrameLoader frameLoader = new FrameLoader();
				AnimationDefinition frame = frameLoader.load(framemap, archiveFile.getFileId(), contents);
				animations.add(frame);
            }

            frames = new FramesDefinition(animations.toArray(AnimationDefinition[]::new));
			framesCache.put(id, frames);
        } catch (IOException e) {
            return null;
        }
        return frames;
    }

    public ModelDefinition transformObjectModel(ModelDefinition modelDefinition, int frame, int orientation) {
        frame = frameIds[frame];
        FramesDefinition frames = getFrames(frame >> 16);
		frame &= 65535;
        if (frames == null) {
            return modelDefinition;
        }

        ModelDefinition definition = new ModelDefinition(modelDefinition, false, false, true);
        orientation &= 3;
        if (orientation == 1) {
            definition.rotateY270Ccw();
        } else if (orientation == 2) {
            definition.rotateY180();
        } else if (orientation == 3) {
            definition.rotateY90Ccw();
        }

        definition.animate(frames, frame);
        if (orientation == 1) {
            definition.rotateY90Ccw();
        } else if (orientation == 2) {
            definition.rotateY180();
        } else if (orientation == 3) {
            definition.rotateY270Ccw();
        }

        return definition;
    }
}

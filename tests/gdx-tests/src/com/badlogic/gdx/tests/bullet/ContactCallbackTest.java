/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.tests.bullet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.bullet.ContactProcessedListenerByValue;
import com.badlogic.gdx.physics.bullet.btCollisionObject;
import com.badlogic.gdx.physics.bullet.btManifoldPoint;
import com.badlogic.gdx.physics.bullet.gdxBulletJNI;
import com.badlogic.gdx.utils.Array;

/** @author Xoppa */
public class ContactCallbackTest extends BaseBulletTest {
	// ContactProcessedListenerXXX is called AFTER the contact is processed.
	// Use ContactAddedListenerXXX to get a callback BEFORE the contact processed, 
	// which allows you to alter the objects/manifold before it's processed. 
	public static class TestContactProcessedListener extends ContactProcessedListenerByValue {
		public Array<BulletEntity> entities;
		public BulletEntity ground;
		@Override
		public boolean onContactProcessed (btManifoldPoint cp, int userValue0, int userValue1) {
			BulletEntity e1 = (BulletEntity)(entities.get(userValue0));
			BulletEntity e2 = (BulletEntity)(entities.get(userValue1));
			if (e1 == ground || e2 == ground) {
				return false;
			}

			int flags1 = e1.body.getCollisionFlags();
			int flags2 = e2.body.getCollisionFlags();

			// In general, the first (userValue0) object is the object that triggered the callback.
			if ((flags1 & btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK) == btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK) {
				e1.body.setCollisionFlags(flags1 & (-1 ^ btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK));
				e1.color.set(Color.RED);
			}
			// The second object might also have the callback enabled.
			if ((flags2 & btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK) == btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK) {
				e2.body.setCollisionFlags(flags2 & (-1 ^ btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK));
				e2.color.set(Color.RED);
			}
			return false;
		}
	}
	
	final int BOXCOUNT_X = 5;
	final int BOXCOUNT_Y = 1;
	final int BOXCOUNT_Z = 5;

	final float BOXOFFSET_X = -5f;
	final float BOXOFFSET_Y = 0.5f;
	final float BOXOFFSET_Z = -5f;
	
	protected BulletEntity ground;
	
	TestContactProcessedListener contactProcessedListener;
	
	@Override
	public void create () {
		super.create();
		
		// Create the entities
		(ground = world.add("ground", 0f, 0f, 0f))
			.color.set(0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(), 1f);
		
		for (int x = 0; x < BOXCOUNT_X; x++) {
			for (int y = 0; y < BOXCOUNT_Y; y++) {
				for (int z = 0; z < BOXCOUNT_Z; z++) {
					final BulletEntity e = (BulletEntity)world.add("box", BOXOFFSET_X + x * 2f, BOXOFFSET_Y + y * 2f, BOXOFFSET_Z + z * 2f);
					e.color.set(0.5f + 0.5f * (float)Math.random(), 0.5f + 0.5f * (float)Math.random(), 0.5f + 0.5f * (float)Math.random(), 1f);
					// Set the collision flags to include CF_CUSTOM_MATERIAL_CALLBACK to receive contact callbacks for that collision object.
					e.body.setCollisionFlags(e.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
				}
			}
		}
		
		// Creating a contact listener, also enables that particular type of contact listener and sets it active.
		contactProcessedListener = new TestContactProcessedListener();
		contactProcessedListener.entities = world.entities;
		contactProcessedListener.ground = ground;
	}
	
	@Override
	public boolean tap (float x, float y, int count, int button) {
		shoot(x, y);
		return true;
	}
	
	@Override
	public void dispose () {
		// Deleting the active contact listener, also disables that particular type of contact listener.
		if (contactProcessedListener != null)
			contactProcessedListener.delete();
		contactProcessedListener = null;
		super.dispose();
		ground = null;
	}
}
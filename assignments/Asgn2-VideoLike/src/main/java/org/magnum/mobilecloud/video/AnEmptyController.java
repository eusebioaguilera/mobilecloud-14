/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.magnum.mobilecloud.video;

import java.security.Principal;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.magnum.mobilecloud.video.auth.OAuth2SecurityConfiguration;
import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;

import com.google.common.collect.Lists;

@Controller
public class AnEmptyController {
	
	/**
	 * You will need to create one or more Spring controllers to fulfill the
	 * requirements of the assignment. If you use this file, please rename it
	 * to something other than "AnEmptyController"
	 * 
	 * 
		 ________  ________  ________  ________          ___       ___  ___  ________  ___  __       
		|\   ____\|\   __  \|\   __  \|\   ___ \        |\  \     |\  \|\  \|\   ____\|\  \|\  \     
		\ \  \___|\ \  \|\  \ \  \|\  \ \  \_|\ \       \ \  \    \ \  \\\  \ \  \___|\ \  \/  /|_   
		 \ \  \  __\ \  \\\  \ \  \\\  \ \  \ \\ \       \ \  \    \ \  \\\  \ \  \    \ \   ___  \  
		  \ \  \|\  \ \  \\\  \ \  \\\  \ \  \_\\ \       \ \  \____\ \  \\\  \ \  \____\ \  \\ \  \ 
		   \ \_______\ \_______\ \_______\ \_______\       \ \_______\ \_______\ \_______\ \__\\ \__\
		    \|_______|\|_______|\|_______|\|_______|        \|_______|\|_______|\|_______|\|__| \|__|
                                                                                                                                                                                                                                                                        
	 * 
	 */
	
	/*
	 * The repository
	 * */
	@Autowired
	private VideoRepository videos;
	
	@RequestMapping(value="/go",method=RequestMethod.GET)
	public @ResponseBody String goodLuck(){
		return "Good Luck!";
	}
	
	/*
	 * This method implements the functionality of adding a new video
	 * */
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v){
		videos.save(v);
		return v;
	}
	
	/*
	 * This method implements the functionality of return the list of videos 
	 * 
	 * */
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList() {
		Iterable<Video> iter = videos.findAll();
		return Lists.newArrayList(iter);
	}
	
	/*
	 * This method implements the functionality of return a video by its identificator
	 * 
	 * */
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}", method=RequestMethod.GET)
	public @ResponseBody Video getVideoById(@PathVariable("id") long id,HttpServletResponse response) {
		Video v = videos.findOne(id);
		
		if (v == null) {
			response.setStatus(HttpStatus.NOT_FOUND.value());
		}
		
		return v;
	}
	
	
	/*
	 * This method returns all the videos with a "name" like title 
	 * 
	 * */
	@RequestMapping(value=VideoSvcApi.VIDEO_TITLE_SEARCH_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> findByTitle(@RequestParam(VideoSvcApi.TITLE_PARAMETER) String title) {
		return videos.findByName(title);
	}
	
	/*
	 * This method returns a collection of videos with a duration less or equal to "duration" param 
	 * 
	 * */
	@RequestMapping(value=VideoSvcApi.VIDEO_DURATION_SEARCH_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> findByDurationLessThan(@RequestParam(VideoSvcApi.DURATION_PARAMETER) long duration) {
		return videos.findByDurationLessThan(duration);
	}
	
	
	/*
	 * This method adds a like to a video by the user "username"
	 * 
	 * */
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}/like", method=RequestMethod.POST)
	public void likeVideo(@PathVariable("id") long id, Principal p, HttpServletResponse response) {
		// Username
		String username = p.getName();
		Video v = videos.findOne(id);
		
		if (v == null) {
			response.setStatus(HttpStatus.NOT_FOUND.value());
		} else {
		
			Collection<String> names = v.getLikesBy();
			
			if (names.contains(username)) {
				response.setStatus(HttpStatus.BAD_REQUEST.value());
			} else {
				names.add(username);
				v.setLikesBy(names);
				//videos.delete(v);
				v.setLikes(names.size());
				videos.save(v);
				response.setStatus(HttpStatus.OK.value());
			}
		}
	}
	
	
	/*
	 * This method removes the like of a video by a user "username"
	 * 
	 * */
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}/unlike", method=RequestMethod.POST)
	public void unlikeVideo(@PathVariable("id") long id, Principal p, HttpServletResponse response) {
		// Username
		String username = p.getName();
		Video v = videos.findOne(id);

		if (v == null) {
			response.setStatus(HttpStatus.NOT_FOUND.value());
		} else {
			Collection<String> names = v.getLikesBy();
			
			if (!names.contains(username)) {
				response.setStatus(HttpStatus.BAD_REQUEST.value());
			} else {
				names.remove(username);
				v.setLikesBy(names);
				v.setLikes(names.size());
				videos.save(v);
				response.setStatus(HttpStatus.OK.value());
			}
		}
	}
	
	/*
	 * This method returns the users that like a video 
	 * 
	 * */
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}/likedby", method=RequestMethod.GET)
	public @ResponseBody Collection<String> getUsersWhoLikedVideo(@PathVariable("id") long id, HttpServletResponse response) {
		Video v = videos.findOne(id);
		
		if (v == null) {
			response.setStatus(HttpStatus.NOT_FOUND.value());
		} else {
			response.setStatus(HttpStatus.OK.value());
		}
		
		
		return v.getLikesBy();
	}
}

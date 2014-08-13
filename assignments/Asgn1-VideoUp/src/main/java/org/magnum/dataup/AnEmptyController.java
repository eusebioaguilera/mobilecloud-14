/*
 * 
 * Copyright 2014 Jules White, Eusebio Aguilera
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
package org.magnum.dataup;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import retrofit.client.Response;

@Controller
public class AnEmptyController {
	
	/*
	 * Constructor initializes videoManager and videos HashMap
	 */
	public AnEmptyController() throws IOException {
		super();
		this.videoDataMgr = VideoFileManager.get();
		this.videos = new HashMap<Long, Video>();
	}
	
	// Atomic Integer to create unique IDs
	private static final AtomicLong currentId =  new AtomicLong(0L);; 
	private Map<Long, Video> videos;
	
	private VideoFileManager videoDataMgr;
	
	/*
	 * This controller returns the list of current videos on the server
	 */
	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList() {
		return videos.values();
	}

	/*
	 * This controller adds a video to the server side. The controller creates an unique ID 
	 * for the video
	 */
	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v) {
		// Generate id
		checkAndSetId(v);
		v.setDataUrl(getDataUrl(v.getId()));
		videos.put(v.getId(), v);
		return v;
	}

	/*
	 * This methods checks if the parameter video has a valid ID, if not a valid and unique 
	 * ID is set
	 */
	private void checkAndSetId(Video v) {
		if (v.getId() == 0) {
			v.setId(currentId.incrementAndGet());
		}
	}

	/*
	 * This controller set a video file to a previously created video object. The method checks
	 * if the ID corresponds to a previously created video object. If not a 404 message is returned
	 * to the client 
	 */
	@RequestMapping(value = VideoSvcApi.VIDEO_DATA_PATH, method = RequestMethod.POST)
	public @ResponseBody VideoStatus setVideoData(@PathVariable(VideoSvcApi.ID_PARAMETER) long id, @RequestParam(VideoSvcApi.DATA_PARAMETER) MultipartFile videoData, HttpServletResponse response) throws IOException {
		VideoStatus s = new VideoStatus(VideoStatus.VideoState.READY);
		
		if (videos.containsKey(id)) {
			Video v = videos.get(id);
			saveSomeVideo(v, videoData);
			v.setDataUrl(getDataUrl(id));
			videos.put(id, v);
		} else {
			/// Not found 404
			response.setStatus(HttpStatus.NOT_FOUND.value());
		}
		
		return s;
	}
	
	public void saveSomeVideo(Video v, MultipartFile videoData)
			throws IOException {

		videoDataMgr.saveVideoData(v, videoData.getInputStream());

	}

	
	/*
	 * This controller get the video file associated to a video object (ID) in the server. If 
	 * the ID is not found in the server an 404 error is return to the client 
	 */
	@RequestMapping(value = VideoSvcApi.VIDEO_DATA_PATH, method = RequestMethod.GET)
	public @ResponseBody Response getData(@PathVariable long id, HttpServletResponse response) throws IOException {
		//return null;
		if (videos.containsKey(id)) {
			Video v = videos.get(id);
			if (videoDataMgr.hasVideoData(v)) {
				response.setStatus(HttpStatus.OK.value());
				// Copy the video file as binary in the response object
				videoDataMgr.copyVideoData(v, response.getOutputStream());
			} else {
				response.setStatus(HttpStatus.NOT_FOUND.value());
			}
		} else {
			response.setStatus(HttpStatus.NOT_FOUND.value());
		}
		
		// The video is return in the response object 
		return null;
	}
	
	// Get the complete URL of the video on the server
	private String getDataUrl(long videoId) {

		String url = getUrlBaseForLocalServer() + "/video/" + videoId + "/data";

		return url;

	}

	// Base URL of the server
	private String getUrlBaseForLocalServer() {

		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
				.getRequestAttributes()).getRequest();

		String base =

		"http://"
				+ request.getServerName()

				+ ((request.getServerPort() != 80) ? ":"
						+ request.getServerPort() : "");

		return base;

	}

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
	
}

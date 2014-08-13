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
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import retrofit.client.Response;
import retrofit.mime.TypedFile;

@Controller
public class AnEmptyController {
	
	public AnEmptyController() throws IOException {
		super();
		this.videoDataMgr = VideoFileManager.get();
		this.videos = new HashMap<Long, Video>();
	}

	private static final AtomicLong currentId =  new AtomicLong(0L);; 
	private Map<Long, Video> videos;
	
	private VideoFileManager videoDataMgr;
	
	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList() {
		// TODO Auto-generated method stub
		return videos.values();
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v) {
		// TODO Auto-generated method stub
		// Generate id
		checkAndSetId(v);
		v.setDataUrl(getDataUrl(v.getId()));
		videos.put(v.getId(), v);
		return v;
	}

	private void checkAndSetId(Video v) {
		if (v.getId() == 0) {
			v.setId(currentId.incrementAndGet());
		}
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_DATA_PATH, method = RequestMethod.POST)
	public @ResponseBody VideoStatus setVideoData(@PathVariable(VideoSvcApi.ID_PARAMETER) long id, @RequestParam(VideoSvcApi.DATA_PARAMETER) MultipartFile videoData, HttpServletResponse response) throws IOException {
		// TODO Auto-generated method stub
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

	@RequestMapping(value = VideoSvcApi.VIDEO_DATA_PATH, method = RequestMethod.GET)
	public @ResponseBody Response getData(@PathVariable long id, HttpServletResponse response) throws IOException {
		// TODO Auto-generated method stub
		//return null;
		if (videos.containsKey(id)) {
			Video v = videos.get(id);
			if (videoDataMgr.hasVideoData(v)) {
				response.setStatus(HttpStatus.OK.value());
				videoDataMgr.copyVideoData(v, response.getOutputStream());
			} else {
				response.setStatus(HttpStatus.NOT_FOUND.value());
			}
		} else {
			response.setStatus(HttpStatus.NOT_FOUND.value());
		}
		
		return null;
	}
	
	private String getDataUrl(long videoId) {

		String url = getUrlBaseForLocalServer() + "/video/" + videoId + "/data";

		return url;

	}

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

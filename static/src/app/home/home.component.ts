import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';

import { User } from '../_models/index';
import { UserService, AlertService, SimpleRegistrationService } from '../_services/index';
import {VgAPI} from "videogular2/core";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
  providers: [SimpleRegistrationService]
})
export class HomeComponent implements OnInit {

  currentUser: User;
  users: User[] = [];
  loading = false;
  loaded = false;
  video = "sample.mp4";
  videoUrl = "";
  api: VgAPI;

  constructor(private userService: UserService,
              private simpleRegistrationService: SimpleRegistrationService,
              private alertService: AlertService) {
    this.currentUser = JSON.parse(localStorage.getItem('currentUser'));
  }

  ngOnInit() {

  }

  onPlayerReady(api: VgAPI) {
    this.api = api;
    this.api.getDefaultMedia().subscriptions.loadedMetadata.subscribe(
      this.playVideoPlayer.bind(this)
    );
  }

  playVideo() {
    this.loading = true;
    this.simpleRegistrationService.getPreSignedURL(this.video)
      .subscribe(
        data => {
          if (data.valid){
            this.videoUrl = data.url;
            this.loaded = true;
          } else {
            this.loading = false; // return the play button
          }

        },
        error => {
          this.alertService.error(error);
          this.loading = false;
        });

  }

  playVideoPlayer() {
    this.api.play();
  }

}

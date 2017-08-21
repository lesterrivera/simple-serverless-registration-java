import { Injectable } from '@angular/core';
import {Http, Headers, Response, RequestOptions} from '@angular/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map'

import { environment } from '../../environments/environment';


@Injectable()
export class SimpleRegistrationService {

  private apiUrl = environment.apiUrl;

  constructor(private http: Http) {}

  register(email: string) {
    var options = new RequestOptions({
      headers: new Headers({
        'Content-Type': 'application/json'
      })
    })
    return this.http.post(`${this.apiUrl}/api/register`, JSON.stringify({ email: email}), options)
      .map((response: Response) => {
        // login successful if there's a jwt token in the response
        let res = response.json();
        let user = res.input;
        if (user && user.token) {
          // store user details and jwt token in local storage to keep user logged in between page refreshes
          localStorage.setItem('currentUser', JSON.stringify(user));
        }

        return user;
      });
  }

  logout() {
    // remove user from local storage to log user out
    localStorage.removeItem('currentUser');
  }

  confirm(email: string, verifyToken: string) {
    // Confirm the email address
    return this.http.post(`${this.apiUrl}/api/confirm`, JSON.stringify({ email: email, verifyToken: verifyToken}), this.jwt())
      .map((response: Response) => {
        // login successful if there's a jwt token in the response
        console.log(response);
        let res = response.json();
        let user = res.input;
        if (user && user.isVerified) {
          // store user details in local storage to keep user logged in between page refreshes
          localStorage.setItem('currentUser', JSON.stringify(user));
        }

        return user;
      });
  }

  getPreSignedURL(myUri: string) {
    // Get an authorized urls
    return this.http.post(`${this.apiUrl}/api/sign`, JSON.stringify({uri: myUri}), this.jwt())
      .map((response: Response) => {
        let res = response.json();
        let video = res.input;
        video.valid = false;
        if (video && video.url) {
          // add a attribute to make easier to validate
          video.valid = true;
        }
        return video;
      });
  }

  // private helper methods

  private jwt() {
    // create authorization header with jwt token
    let currentUser = JSON.parse(localStorage.getItem('currentUser'));
    if (currentUser && currentUser.token) {
      let headers = new Headers({ 'Authorization': 'Bearer ' + currentUser.token });
      headers.append('Content-Type', 'application/json');
      return new RequestOptions({ headers: headers });
    }
  }
}

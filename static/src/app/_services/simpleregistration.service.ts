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
    });

    return this.http.post(`${this.apiUrl}/api/register`, JSON.stringify({ email: email}), options)
      .map((response: Response) => {
        // login successful if there's a jwt token in the response
        console.log(response);
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
  }

  getPreSignedURL(uri: string) {
    // Get an authorized urls
  }
}

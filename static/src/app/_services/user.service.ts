import { Injectable } from '@angular/core';
import { Http, Headers, RequestOptions, Response } from '@angular/http';

import { User } from '../_models/index';

import { environment } from '../../environments/environment';

@Injectable()
export class UserService {

  private apiUrl = environment.apiUrl;

  constructor(private http: Http) { }

  // Get the user
  getByEmail(email: string) {
    return this.http.get(`${this.apiUrl}/api/users/` + email, this.jwt()).map((response: Response) => response.json());
  }

  // Register the user's email
  register(user: User) {
    return this.http.post(`${this.apiUrl}/api/users`, user, this.jwt()).map((response: Response) => response.json());
  }

  // Confirm the user's email
  confirm(user: User) {
    return this.http.put(`${this.apiUrl}/api/users/` + user.email, user, this.jwt()).map((response: Response) => response.json());
  }

  // Delete the local token
  delete(id: number) {
    return this.http.delete(`${this.apiUrl}/api/users/` + id, this.jwt()).map((response: Response) => response.json());
  }

  // Delete the local token
  GetPreSignedUrl(uri: string) {
    return this.http.delete(`${this.apiUrl}/api/users/` + uri, this.jwt()).map((response: Response) => response.json());
  }

  // private helper methods

  private jwt() {
    // create authorization header with jwt token
    let currentUser = JSON.parse(localStorage.getItem('currentUser'));
    if (currentUser && currentUser.token) {
      let headers = new Headers({ 'Authorization': 'Bearer ' + currentUser.token });
      return new RequestOptions({ headers: headers });
    }
  }
}

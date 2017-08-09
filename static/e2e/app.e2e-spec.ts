import { SimpleServerlessRegistrationAppPage } from './app.po';

describe('simple-serverless-registration-app App', () => {
  let page: SimpleServerlessRegistrationAppPage;

  beforeEach(() => {
    page = new SimpleServerlessRegistrationAppPage();
  });

  it('should display welcome message', done => {
    page.navigateTo();
    page.getParagraphText()
      .then(msg => expect(msg).toEqual('Welcome to app!!'))
      .then(done, done.fail);
  });
});

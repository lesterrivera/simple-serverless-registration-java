import {
  Component, OnInit,
  trigger, state, animate, transition, style, Input
} from '@angular/core';


@Component({
  selector: 'toggle',
  templateUrl: './toggle.component.html',
  styleUrls: ['./toggle.component.css'],
  animations: [
    trigger('toggleState', [
      // What happens when toggleState is true
      state('true' , style({ maxHeight: '200px' })),
      // What happens when toggleState is false
      state('false', style({ maxHeight: 0, padding: 0, display: 'none' })),
      transition('* => *', animate('300ms')),
    ])
  ],
})
export class ToggleComponent implements OnInit {

  @Input() shouldToggle = false;

  constructor() { }

  ngOnInit() {
  }

}

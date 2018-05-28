import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'identity',
  templateUrl: './id-card.component.html',
  styleUrls: ['./id-card.component.css']
})
export class IdCardComponent implements OnInit {

  constructor() { }

  ngOnInit() {
  }
  

    title = 'CV';
    Nom = 'Régis';
    Prenom = 'Lévêque';
    Sexe = 'Masculin';
    Emploi = 'Développeur';
    Age = 35 ;

}

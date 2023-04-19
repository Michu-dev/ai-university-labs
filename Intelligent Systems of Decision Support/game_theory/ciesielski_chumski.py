import numpy as np
import random
from player import Player

class Ciesielski_Chumski(Player):
    def startGame(self, cards):
        super().startGame(cards)
        self.opponent_cards = []
        self.opponent_cards_on_table = []
        self.my_cards_on_table = []
        self.checked = None

    def putCard(self, declared_card):
        if declared_card is not None:
            self.opponent_cards_on_table.append(declared_card)
            if declared_card in self.opponent_cards:
                self.opponent_cards.remove(declared_card)

            for card in sorted(self.cards, key=lambda x: x[0]):
                if card[0] >= declared_card[0]:
                    self.my_cards_on_table.append(card)
                    return card, card
        else:
            if self.checked == False:
                cards_taken = []
                cards_taken.append(self.opponent_cards_on_table[-1:])
                cards_taken.append(self.my_cards_on_table[max([-2, -len(self.my_cards_on_table)]):])
                self.opponent_cards.extend(cards_taken)
                self.opponent_cards_on_table = self.opponent_cards_on_table[:-1]
                self.my_cards_on_table = self.my_cards_on_table[:max([-2, -len(self.my_cards_on_table)])]
            card = min(self.cards, key=lambda x: x[0])
            self.my_cards_on_table.append(card)
            return card, card
        
    
    def getCheckFeedback(self, checked, iChecked, iDrewCards, revealedCard, noTakenCards, log=True):
        super().getCheckFeedback(checked, iChecked, iDrewCards, revealedCard, noTakenCards, log)
        self.checked = checked
        if checked and iChecked and iDrewCards:
            self.opponent_cards_on_table = self.opponent_cards_on_table[:max([-2, -len(self.opponent_cards_on_table)])]
            self.my_cards_on_table = self.my_cards_on_table[:-1]
        elif checked and not iChecked and iDrewCards:
            self.opponent_cards_on_table = self.opponent_cards_on_table[:-1]
            self.my_cards_on_table = self.my_cards_on_table[:max([-2, -len(self.my_cards_on_table)])]
        elif checked and iChecked and not iDrewCards:
            cards_taken = []
            cards_taken.append(self.opponent_cards_on_table[max([-2, -len(self.opponent_cards_on_table)]):])
            cards_taken.append(self.my_cards_on_table[-1:])
            self.opponent_cards.extend(cards_taken)
            self.opponent_cards_on_table = self.opponent_cards_on_table[:max([-2, -len(self.opponent_cards_on_table)])]
            self.my_cards_on_table = self.my_cards_on_table[:-1]
        elif checked and not iChecked and not iDrewCards:
            cards_taken = []
            cards_taken.append(self.opponent_cards_on_table[-1:])
            cards_taken.append(self.my_cards_on_table[max([-2, -len(self.my_cards_on_table)]):])
            self.opponent_cards.extend(cards_taken)
            self.opponent_cards_on_table = self.opponent_cards_on_table[:-1]
            self.my_cards_on_table = self.my_cards_on_table[:max([-2, -len(self.my_cards_on_table)])]

    def checkCard(self, opponent_declaration):
        if opponent_declaration in self.cards or opponent_declaration in self.my_cards_on_table or opponent_declaration in self.opponent_cards_on_table or max(self.cards, key=lambda x: x[0])[0] < opponent_declaration[0]:
            return True
        return False
        
    def takeCards(self, cards_to_take):
        super().takeCards(cards_to_take)
        # usuniecie kart, jesli przeciwnik oszukiwal
        for card in cards_to_take:
            if card in self.opponent_cards:
                self.opponent_cards.remove(card)
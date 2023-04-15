import numpy as np
from player import Player

class Nazwisko(Player):
    
     ### player's random strategy
    def putCard(self, declared_card):

        ### check if must draw
        if len(self.cards) == 1 and declared_card is not None and self.cards[0][0] < declared_card[0]:
            return "draw"

        powerful_cards = []
        # if player has better card than declared one, put this on the table
        cards = np.array(self.cards)
        if declared_card is not None:
            powerful_cards = cards[np.where(cards > declared_card[0])[0]]
            
        
        if len(powerful_cards) > 0:
            card_idx = powerful_cards.argmin(axis=0)
            card = tuple(powerful_cards[card_idx[0]])
            return card, card
        
        else:
            card = min(self.cards, key=lambda x: x[0])
            declaration = (card[0], card[1])
            if declared_card is not None:
                min_val = declared_card[0]
                declaration = (min(min_val + 1, 14), declaration[1])
            return card, declaration
    
    ### randomly decides whether to check or not
    def checkCard(self, opponent_declaration):
        if opponent_declaration in self.cards: return True
        if len(self.cards) > 8:
            return np.random.choice([True, False], p=[0.7, 0.3])
        return np.random.choice([True, False], p=[0.3, 0.7])
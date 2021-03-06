/*
 *  Copyright 2010 BetaSteward_at_googlemail.com. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY BetaSteward_at_googlemail.com ``AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL BetaSteward_at_googlemail.com OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and should not be interpreted as representing official policies, either expressed
 *  or implied, of BetaSteward_at_googlemail.com.
 */
package mage.cards.g;

import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.filter.common.FilterControlledLandPermanent;
import mage.filter.common.FilterLandPermanent;
import mage.filter.predicate.mageobject.SubtypePredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.Target;
import mage.target.common.TargetControlledPermanent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author Markedagain
 */
public class GlobalRuin extends CardImpl {

    public GlobalRuin(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.SORCERY},"{4}{W}");

        // Each player chooses from the lands he or she controls a land of each basic land type, then sacrifices the rest.
        this.getSpellAbility().addEffect(new GlobalRuinDestroyLandEffect());
    }

    public GlobalRuin(final GlobalRuin card) {
        super(card);
    }

    @Override
    public GlobalRuin copy() {
        return new GlobalRuin(this);
    }
}

class GlobalRuinDestroyLandEffect extends OneShotEffect {

    public GlobalRuinDestroyLandEffect() {
        super(Outcome.DestroyPermanent);
        this.staticText = "Each player chooses from the lands he or she controls a land of each basic land type, then sacrifices the rest";
    }

    public GlobalRuinDestroyLandEffect(final GlobalRuinDestroyLandEffect effect) {
        super(effect);
    }

    @Override
    public GlobalRuinDestroyLandEffect copy() {
        return new GlobalRuinDestroyLandEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Set<UUID> lands = new HashSet<>();
        
        for (UUID playerId : game.getState().getPlayersInRange(source.getControllerId(), game)) {
            Player player = game.getPlayer(playerId);
                for (String landName : new String[]{"Forest", "Island", "Mountain", "Plains", "Swamp"}) {
                    FilterControlledLandPermanent filter = new FilterControlledLandPermanent(landName + " you control");
                    filter.add(new SubtypePredicate(landName));
                    Target target = new TargetControlledPermanent(1, 1, filter, true);
                    if (target.canChoose(player.getId(), game)) {
                        player.chooseTarget(outcome, target, source, game);
                        lands.add(target.getFirstTarget());
                    }
                }             
        }
        for (Permanent permanent : game.getBattlefield().getAllActivePermanents(new FilterLandPermanent(), game)){
            if (!lands.contains(permanent.getId())){
                permanent.sacrifice(permanent.getId(), game);
            }
        }
        return true;
    }
}

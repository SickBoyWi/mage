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
package mage.cards.u;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import mage.MageObject;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.AsThoughEffectImpl;
import mage.abilities.effects.ReplacementEffectImpl;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.AsThoughEffectType;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.WatcherScope;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.players.Player;
import mage.watchers.Watcher;

/**
 *
 * @author LevelX2
 */
public class UbaMask extends CardImpl {

    public UbaMask(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ARTIFACT}, "{4}");

        // If a player would draw a card, that player exiles that card face up instead.
        this.addAbility(new SimpleStaticAbility(Zone.BATTLEFIELD, new UbaMaskReplacementEffect()));

        // Each player may play cards he or she exiled with Uba Mask this turn.
        this.addAbility(new SimpleStaticAbility(Zone.BATTLEFIELD, new UbaMaskPlayEffect()), new UbaMaskExiledCardsWatcher());
    }

    public UbaMask(final UbaMask card) {
        super(card);
    }

    @Override
    public UbaMask copy() {
        return new UbaMask(this);
    }
}

class UbaMaskReplacementEffect extends ReplacementEffectImpl {

    UbaMaskReplacementEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Neutral);
        this.staticText = "If a player would draw a card, that player exiles that card face up instead";
    }

    UbaMaskReplacementEffect(final UbaMaskReplacementEffect effect) {
        super(effect);
    }

    @Override
    public UbaMaskReplacementEffect copy() {
        return new UbaMaskReplacementEffect(this);
    }

    @Override
    public boolean replaceEvent(GameEvent event, Ability source, Game game) {
        MageObject sourceObject = source.getSourceObject(game);
        Player player = game.getPlayer(event.getPlayerId());
        if (player != null && sourceObject != null) {
            Card card = player.getLibrary().getFromTop(game);
            if (card != null) {
                if (player.moveCardsToExile(card, source, game, true, source.getSourceId(), sourceObject.getIdName())) {
                    UbaMaskExiledCardsWatcher watcher = (UbaMaskExiledCardsWatcher) game.getState().getWatchers().get(UbaMaskExiledCardsWatcher.class.getName());
                    if (watcher != null) {
                        watcher.addExiledCard(event.getPlayerId(), card, game);
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DRAW_CARD;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        return true;
    }
}

class UbaMaskPlayEffect extends AsThoughEffectImpl {

    public UbaMaskPlayEffect() {
        super(AsThoughEffectType.PLAY_FROM_NOT_OWN_HAND_ZONE, Duration.EndOfGame, Outcome.Benefit);
        staticText = "Each player may play cards he or she exiled with {this} this turn";
    }

    public UbaMaskPlayEffect(final UbaMaskPlayEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return true;
    }

    @Override
    public UbaMaskPlayEffect copy() {
        return new UbaMaskPlayEffect(this);
    }

    @Override
    public boolean applies(UUID objectId, Ability source, UUID affectedControllerId, Game game) {
        Card card = game.getCard(objectId);
        if (card != null
                && affectedControllerId.equals(card.getOwnerId())
                && game.getState().getZone(card.getId()) == Zone.EXILED) {
            UbaMaskExiledCardsWatcher watcher = (UbaMaskExiledCardsWatcher) game.getState().getWatchers().get(UbaMaskExiledCardsWatcher.class.getName());
            if (watcher != null) {
                List<MageObjectReference> exiledThisTurn = watcher.getUbaMaskExiledCardsThisTurn(affectedControllerId);
                return exiledThisTurn != null
                        && exiledThisTurn.contains(new MageObjectReference(card, game));
            }
        }
        return false;
    }
}

class UbaMaskExiledCardsWatcher extends Watcher {

    private final HashMap<UUID, List<MageObjectReference>> exiledCards = new HashMap<>();

    public UbaMaskExiledCardsWatcher() {
        super(UbaMaskExiledCardsWatcher.class.getName(), WatcherScope.GAME);
    }

    public UbaMaskExiledCardsWatcher(final UbaMaskExiledCardsWatcher watcher) {
        super(watcher);
        exiledCards.putAll(watcher.exiledCards);
    }

    @Override
    public UbaMaskExiledCardsWatcher copy() {
        return new UbaMaskExiledCardsWatcher(this);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        // no events to watch
    }

    @Override
    public void reset() {
        super.reset();
        exiledCards.clear();
    }

    public void addExiledCard(UUID playerId, Card card, Game game) {
        List<MageObjectReference> exiledCardsByPlayer;
        if (exiledCards.containsKey(playerId)) {
            exiledCardsByPlayer = exiledCards.get(playerId);
        } else {
            exiledCardsByPlayer = new ArrayList<>();
        }
        exiledCardsByPlayer.add(new MageObjectReference(card, game));
        exiledCards.put(playerId, exiledCardsByPlayer);
    }

    public List<MageObjectReference> getUbaMaskExiledCardsThisTurn(UUID playerId) {
        return exiledCards.get(playerId);
    }
}

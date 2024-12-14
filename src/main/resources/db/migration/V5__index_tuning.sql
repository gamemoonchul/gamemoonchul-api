CREATE INDEX idx_match_game_game_id ON match_game(game_id);

CREATE INDEX idx_match_user_game_id ON match_user(game_id);

CREATE INDEX idx_vote_option_post_id ON vote_option(post_id);

CREATE INDEX idx_member_id ON comment(member_id);
CREATE INDEX idx_post_id ON comment(post_id);

CREATE INDEX idx_vote_options_id ON vote(vote_options_id);


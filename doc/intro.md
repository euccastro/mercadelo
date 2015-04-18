# Design & implementation notes

This is not documentation for the end user; this project is not nearly there yet.  Also, this offers little in the way of rationale.  It only tries to describe how things are supposed to work eventually, in enough detail that this spec can be cross-referenced with unit tests as functionality is implemented.

I'm assuming basic familiarity with complementary currencies.  The links in the [README](https://github.com/euccastro/mercadelo#mercadelo) should be a good starting point.

## Basic concepts

Like in a LETS, all users start with an account with zero balance.  If Al then pays Beth 5 units of currency (we call them "elos"), Al's account is charged 5 elos and Beth's account is credited the same amount.  This operation may possibly bring Al's account into negative numbers.  The system allows this and it imposes no limit on how far into "debt" a user may become.  Other users may refuse to accept Al's business if they perceive he is freeloading.  We'll see how the system enables and promotes this.

Unlike in traditional mutual credit systems, each user has their own currency.  So in the example above, to be precise we must specify that 5 of "Al's elos" have been transferred.  If Charles pays some additional 10 of his elos to Beth, the system keeps track of "Charles' elos" separately from "Al's elos" in Beth's account.  Any user can choose to refuse any other user's currency.

The most primitive and manual mode of operation works like this: Al wants to buy 5 elos worth of stuff from Beth.  So he makes an offer to her for a total of that quantity.  The offer may include a combination of Al's own currency and/or any other currencies that Al has.  Al can't offer more than he has of anyone else's currency, but he can offer his own currency with no limit.

When Beth receives such an offer, she can pick any combination of the offered currencies, adding up to the amount that Al specified.  Alternatively, she can refuse the payment altogether.  She can't accept the payment partially.

As a corollary of the above, users have zero or negative balance in their own currency, and zero or positive balance in everyone else's currency.

This manual way of offering and accepting payments is slow and laborious.  For this and other reasons, the system offers users the option of automatically accepting payments in certain currencies up to certain amounts.

For example, if Beth declares that the automatically accepts up to 10 of Al's elos, then Al (or anyone who has Al's elos) can pay Beth instantly and without her intervention, until her account indeed has 10 of Al's elos, at which point no more payments will be automatically processed.

In additon, the system can use this information to find indirect payment options.  For example, say Al wants to pay Charlie.  Charlie doesn't accept Al's elos, but he accepts Beth's, who in turn accepts Al's.  The system can exploit such transitive confidence relationships to enable deals that couldn't be made otherwise.

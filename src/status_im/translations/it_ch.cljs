(ns status-im.translations.it-ch)

(def translations
  {
   ;common
   :members-title                         "Membri"
   :not-implemented                       "!non implementato"
   :chat-name                             "Nome chat"
   :notifications-title                   "Notifiche e suoni"
   :offline                               "Offline"

   ;drawer
   :invite-friends                        "Invita amici"
   :faq                                   "FAQ"
   :switch-users                          "Cambia utenti"

   ;chat
   :is-typing                             "sta scrivendo"
   :and-you                               "e tu"
   :search-chat                           "Cerca chat"
   :members                               {:one   "1 membro"
                                           :other "{{count}} membri"
                                           :zero  "nessun membro"}
   :members-active                        {:one   "1 membro, 1 attivo"
                                           :other "{{count}} membri, {{count}} attivi"
                                           :zero  "nessun membro"}
   :active-online                         "Online"
   :active-unknown                        "Sconosciuto"
   :available                             "Disponibile"
   :no-messages                           "Nessun messaggio"
   :suggestions-requests                  "Richieste"
   :suggestions-commands                  "Comandi"

   ;sync
   :sync-in-progress                      "Sincronizzazione in corso..."
   :sync-synced                           "Sincronizzato"

   ;messages
   :status-sending                        "Invio in corso"
   :status-pending                        "In attesa di"
   :status-sent                           "Inviato"
   :status-seen-by-everyone               "Visto da tutti"
   :status-seen                           "Visto"
   :status-delivered                      "Consegnato"
   :status-failed                         "Invio fallito"

   ;datetime
   :datetime-second                       {:one   "secondo"
                                           :other "secondi"}
   :datetime-minute                       {:one   "minuto"
                                           :other "minuti"}
   :datetime-hour                         {:one   "ora"
                                           :other "ore"}
   :datetime-day                          {:one   "giorno"
                                           :other "giorni"}
   :datetime-multiple                     "s"
   :datetime-ago                          "fa"
   :datetime-yesterday                    "ieri"
   :datetime-today                        "oggi"

   ;profile
   :profile                               "Profilo"
   :report-user                           "SEGNALA UTENTE"
   :message                               "Messaggio"
   :username                              "Nome utente"
   :not-specified                         "Non specificato"
   :public-key                            "Chiave pubblica"
   :phone-number                          "Numero di telefono"
   :email                                 "Email"
   :profile-no-status                     "Nessuno stato"
   :add-to-contacts                       "Aggiungi ai contatti"
   :error-incorrect-name                  "Seleziona un altro nome"
   :error-incorrect-email                 "Email errata"

   ;;make_photo
   :image-source-title                    "Immagine profilo"
   :image-source-make-photo               "Scatta"
   :image-source-gallery                  "Seleziona dalla galleria immagini"
   :image-source-cancel                   "Annulla"

   ;sign-up
   :contacts-syncronized                  "I tuoi contatti sono stati sincronizzati"
   :confirmation-code                     (str "Grazie! Ti abbiamo inviato un messaggio con un codice di "
                                               "conferma. Utilizza tale codice per confermare il tuo numero di telefono")
   :incorrect-code                        (str "Il codice inserito ?? errato, riprova")
   :generate-passphrase                   (str "Provveder?? a generare una passphrase cos?? potrai ripristinare il tuo "
                                               "accesso o effettuare il login da un altro dispositivo")
   :phew-here-is-your-passphrase          "*Wow* ?? stato difficile, ecco qui la tua passphrase, *prendi nota e conservala in un luogo sicuro!* Ti servir?? per ripristinare il tuo conto."
   :here-is-your-passphrase               "Ecco qui la tua passphrase, *prendi nota e conservala in un luogo sicuro!* Ti servir?? per ripristinare il tuo conto."
   :written-down                          "Assicurati di averla scritta correttamente"
   :phone-number-required                 "Clicca qui per inserire il tuo numero di telefono e trovare i tuoi amici"
   :intro-status                          "Avvia una conversazione con me per impostare il tuo conto e modificare le tue impostazioni!"
   :intro-message1                        "Benvenuto su Status\nTocca questo messaggio per impostare la tua password e iniziare!"
   :account-generation-message            "Dammi un secondo, devo eseguire dei calcoli matematici complessi per generare il tuo conto!"

   ;chats
   :chats                                 "Conversazioni"
   :new-chat                              "Nuova conversazione"
   :new-group-chat                        "Nuova conversazione di gruppo"

   ;discover
   :discover                              "Scoperta"
   :none                                  "Nessuna"
   :search-tags                           "Inserisci qui i tag di ricerca"
   :popular-tags                          "Tag popolari"
   :recent                                "Recente"
   :no-statuses-discovered                "Nessuno stato identificato"

   ;settings
   :settings                              "Impostazioni"

   ;contacts
   :contacts                              "Contatti"
   :new-contact                           "Nuovo contatto"
   :show-all                              "MOSTRA TUTTI"
   :contacts-group-dapps                  "??Apps"
   :contacts-group-people                 "Persone"
   :contacts-group-new-chat               "Inizia una nuova conversazione"
   :no-contacts                           "Nessun contatto registrato"
   :show-qr                               "Mostra QR"

   ;group-settings
   :remove                                "Rimuovi"
   :save                                  "Salva"
   :change-color                          "Cambia colore"
   :clear-history                         "Cancella cronologia"
   :delete-and-leave                      "Elimina ed esci"
   :chat-settings                         "Impostazioni conversazioni"
   :edit                                  "Modifica"
   :add-members                           "Aggiungi membri"
   :blue                                  "Blu"
   :purple                                "Viola"
   :green                                 "Verde"
   :red                                   "Rosso"

   ;commands
   :money-command-description             "Invia denaro"
   :location-command-description          "Invia posizione"
   :phone-command-description             "Invia numero di telefono"
   :phone-request-text                    "Richiesta numero di telefono"
   :confirmation-code-command-description "Invia codice di conferma"
   :confirmation-code-request-text        "Richiesta codice di conferma"
   :send-command-description              "Invia posizione"
   :request-command-description           "Invia richiesta"
   :keypair-password-command-description  ""
   :help-command-description              "Aiuto"
   :request                               "Richiedi"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH a {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH da {{chat-name}}"

   ;new-group
   :group-chat-name                       "Nome conversazione"
   :empty-group-chat-name                 "Inserire un nome"
   :illegal-group-chat-name               "Selezionare un altro nome"

   ;participants
   :add-participants                      "Aggiungi partecipanti"
   :remove-participants                   "Rimuovi partecipanti"

   ;protocol
   :received-invitation                   "ha ricevuto un invito di conversazione"
   :removed-from-chat                     "ti ha rimosso dalla conversazione di gruppo"
   :left                                  "?? uscito"
   :invited                               "?? stato invitato"
   :removed                               "?? stato rimosso"
   :You                                   "Tu"

   ;new-contact
   :add-new-contact                       "Aggiungi nuovo contatto"
   :import-qr                             "Importa"
   :scan-qr                               "Scansiona QR"
   :name                                  "Nome"
   :whisper-identity                      "Whisper Identity"
   :address-explication                   "Forse qui dovremmo spiegare cos'?? un indirizzo e dove cercarlo"
   :enter-valid-address                   "Inserire un indirizzo valido oppure effettuare la scansione del codice QR"
   :contact-already-added                 "Il contatto ?? gi?? stato aggiunto"
   :can-not-add-yourself                  "Non puoi aggiungere te stesso"
   :unknown-address                       "Indirizzo sconosciuto"


   ;login
   :connect                               "Effettua connessione"
   :address                               "Indirizzo"
   :password                              "Password"
   :login                                 "Login"
   :wrong-password                        "Password errata"

   ;recover
   :recover-from-passphrase               "Ripristina tramite passphrase"
   :recover-explain                       "Inserire la passphrase per ripristinare la password di accesso"
   :passphrase                            "Passphrase"
   :recover                               "Ripristina"
   :enter-valid-passphrase                "Inserire una passphrase"
   :enter-valid-password                  "Inserire una password"

   ;accounts
   :recover-access                        "Ripristina l'accesso"
   :add-account                           "Aggiungi conto"

   ;wallet-qr-code
   :done                                  "OK"
   :main-wallet                           "Wallet principale"

   ;validation
   :invalid-phone                         "Numero di telefono non valido"
   :amount                                "Saldo"
   :not-enough-eth                        (str "Saldo ETH non sufficiente "
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "Conferma transazione"
                                           :other "Conferma {{count}} transazioni"
                                           :zero  "Nessuna transazione"}
   :status                                "Stato"
   :pending-confirmation                  "Conferma pendente"
   :recipient                             "Beneficiario"
   :one-more-item                         "Ancora un elemento"
   :fee                                   "Commissione"
   :value                                 "Valore"

   ;:webview
   :web-view-error                        "Ops, si ?? verificato un errore"

   :confirm                               "Conferma"
   :phone-national                        "Nazionale"
   :transactions-confirmed                {:one   "Transazione confermata"
                                           :other "{{count}} transazioni confermata"
                                           :zero  "Nessuna transazione confermata"}
   :public-group-topic                    "Argomento"
   :debug-enabled                         "Il server di debug ?? stato avviato! Ora puoi aggiungere la tua DApp eseguendo *status-dev-cli scan* dal tuo computer"
   :new-public-group-chat                 "Entra in chat pubblica"
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :sharing-cancel                        "Annulla"
   :share-qr                              "Condividi QR"
   :feedback                              "Hai ricevuto un feedback?\nScuoti il tuo telefono!"
   :twelve-words-in-correct-order         "12 parole in ordine corretto"
   :remove-from-contacts                  "Rimuovi dai contatti"
   :delete-chat                           "Elimina chat"
   :edit-chats                            "Modifica chat"
   :sign-in                               "Accedi"
   :create-new-account                    "Crea nuovo conto"
   :sign-in-to-status                     "Accedi a Stato"
   :got-it                                "OK"
   :move-to-internal-failure-message      "Dobbiamo spostare alcuni file importanti dal dispositivo di archiviazione esterno a quello interno. Per poter completare l'operazione abbiamo bisogno della tua autorizzazione. Non utilizzeremo lo spazio di archiviazione esterno nelle versioni future."
   :edit-group                            "Modifica gruppo"
   :delete-group                          "Elimina gruppo"
   :browsing-title                        "Sfoglia"
   :reorder-groups                        "Riordina gruppi"
   :browsing-cancel                       "Annulla"
   :faucet-success                        "Richiesta faucet ricevuta"
   :choose-from-contacts                  "Scegli dai contatti"
   :new-group                             "Nuovo gruppo"
   :phone-e164                            "Internazionale 1"
   :remove-from-group                     "Rimuovi dal gruppo"
   :search-contacts                       "Cerca tra i contatti"
   :transaction                           "Transazione"
   :public-group-status                   "Pubblico"
   :leave-chat                            "Abbandona chat"
   :start-conversation                    "Inizia conversazione"
   :topic-format                          "Formato errato [a-z0-9\\-]+"
   :enter-valid-public-key                "Inserisci una chiave pubblica valida o scansione un codice QR"
   :faucet-error                          "Errore richiesta faucet"
   :phone-significant                     "Rilevante"
   :search-for                            "Cerca???"
   :sharing-copy-to-clipboard             "Copia negli appunti"
   :phone-international                   "Internazionale 2"
   :enter-address                         "Inserisci indirizzo"
   :send-transaction                      "Invia transazione"
   :delete-contact                        "Elimina contatto"
   :mute-notifications                    "Silenzia le notifiche"


   :contact-s                             {:one   "contatto"
                                           :other "contatti"}
   :group-name                            "Nome del gruppo"
   :next                                  "Avanti"
   :from                                  "Da"
   :search-chats                          "Cerca nelle chat"
   :in-contacts                           "Nei contatti"

   :sharing-share                         "Condividi???"
   :type-a-message                        "Digita un messaggio???"
   :type-a-command                        "Inizia a digitare un comando???"
   :shake-your-phone                      "Hai trovato un bug o hai un suggerimento? ~Scuoti~ il telefono!"
   :status-prompt                         "Crea uno stato per consentire le persone di conoscere le cose che offri. Puoi usare anche gli #hashtag"
   :add-a-status                          "Aggiungi uno stato???"
   :error                                 "Errore"
   :edit-contacts                         "Modifica contatti"
   :more                                  "altro"
   :cancel                                "Annulla"
   :no-statuses-found                     "Nessuno stato trovato"
   :swow-qr                               "Mostra QR"
   :browsing-open-in-web-browser          "Apri nel browser"
   :delete-group-prompt                   "Questo non intaccher?? i contatti"
   :edit-profile                          "Modifica profilo"


   :enter-password-transactions           {:one   "Conferma la transazione inserendo la tua password"
                                           :other "Conferma le transazioni inserendo la tua password"}
   :unsigned-transactions                 "Transazioni non firmate"
   :empty-topic                           "Argomento non definito"
   :to                                    "A"
   :group-members                         "Membri del gruppo"
   :estimated-fee                         "Commissione stimata"
   :data                                  "Dati"})
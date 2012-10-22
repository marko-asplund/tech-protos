
require "liquid"


class TemplateRenderer

  def initialize(data)
    @event_data = data
  end

	def render_template
	  tpl = Liquid::Template.parse(get_template)
	  content = tpl.render('event' => @event_data)
	end
	
	def get_template
    tpl = <<HERE
Commits pushed to the repository contained invalid commit messages.

Please see {{event.repository.url}} for commit message guidelines.

Push event info
***************
repository: {{event.repository.url}}
reference: {{event.ref}}
push date: {{event.head_commit.timestamp | date: ""%Y""}}
pusher: {{event.pusher.name}}

Commits
*******
{% for c in event.commits %}
committed: {{c.committer.username}} / {{c.timestamp}}
commit: {{ c.url }}
message:
{{c.message}}

------
{% endfor %}
HERE
	end

end
